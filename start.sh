#!/usr/bin/env bash
# starts kafka, the backend services and the frontend, then opens the site in the browser
# usage: ./start.sh
#        ./start.sh --services stock,cart     (start only some services)
#        ./start.sh --skip-kafka --no-browser
# service logs are written to .run/logs/, stop everything with: ./stop.sh

set -e

# services the frontend talks to; add payment, chatbot or customer when needed
services="stock,cart,order"
skip_kafka=false
no_browser=false
frontend_port=5500

while [ $# -gt 0 ]; do
    case "$1" in
        --services) services="$2"; shift 2 ;;
        --skip-kafka) skip_kafka=true; shift ;;
        --no-browser) no_browser=true; shift ;;
        --port) frontend_port="$2"; shift 2 ;;
        -h|--help) sed -n '2,6p' "$0"; exit 0 ;;
        *) echo "unknown option: $1" >&2; exit 1 ;;
    esac
done

root="$(cd "$(dirname "$0")" && pwd)"
run_dir="$root/.run"
log_dir="$run_dir/logs"
pid_file="$run_dir/pids.txt"

# plain function instead of an associative array so macOS's bash 3.2 works
port_of() {
    case "$1" in
        stock) echo 8081 ;;
        cart) echo 8082 ;;
        order) echo 8083 ;;
        payment) echo 8084 ;;
        chatbot) echo 8085 ;;
        customer) echo 8080 ;;
        *) return 1 ;;
    esac
}

port_open() {
    (: < "/dev/tcp/127.0.0.1/$1") 2>/dev/null
}

wait_port() {
    local port="$1" name="$2" timeout="${3:-420}"
    local deadline=$(( $(date +%s) + timeout ))
    until port_open "$port"; do
        if [ "$(date +%s)" -gt "$deadline" ]; then
            echo "  WARNING: $name did not start on port $port within $timeout seconds - check $log_dir/$name.log" >&2
            return 0
        fi
        sleep 2
    done
    echo "  $name is up on port $port"
}

# runs the command in the background in its own process group and remembers its pid
start_background() {
    local name="$1" dir="$2"
    shift 2
    (cd "$dir" && exec "$@") > "$log_dir/$name.log" 2>&1 &
    echo "$!" >> "$pid_file"
}

services="$(echo "$services" | tr ',' ' ' | tr '[:upper:]' '[:lower:]')"
for service in $services; do
    if ! port_of "$service" > /dev/null; then
        echo "unknown service '$service' - choose from: stock, cart, order, payment, chatbot, customer" >&2
        exit 1
    fi
done

mkdir -p "$log_dir"
# job control gives each background job its own process group, so stop.sh can stop java too
set -m

# 1. kafka (optional: without it trending books and the order service's stock copy stop updating)
if [ "$skip_kafka" = false ]; then
    echo "Starting Kafka..."
    if ! command -v docker > /dev/null; then
        echo "WARNING: docker was not found - continuing without Kafka" >&2
        skip_kafka=true
    elif ! docker compose -f "$root/docker-compose.yml" up -d --wait; then
        echo "WARNING: Kafka failed to start (is Docker running?) - continuing without it" >&2
        skip_kafka=true
    fi
fi
# the services inherit this, and the stock service then skips sending events
if [ "$skip_kafka" = true ]; then
    export KAFKA_ENABLED=false
else
    export KAFKA_ENABLED=true
fi

# 2. backend services
echo "Starting services: $services"
for service in $services; do
    port="$(port_of "$service")"
    if port_open "$port"; then
        echo "  $service is already running on port $port, skipping"
        continue
    fi
    start_background "$service-service" "$root/services/$service-service" sh ./mvnw spring-boot:run
done

# 3. frontend on port 5500, the origin the services allow
echo "Starting frontend on port $frontend_port..."
python_cmd=""
for candidate in python3 python; do
    # "python3 --version" fails for the windows store placeholder, so it is skipped
    if command -v "$candidate" > /dev/null && "$candidate" --version > /dev/null 2>&1; then
        python_cmd="$candidate"
        break
    fi
done
if port_open "$frontend_port"; then
    echo "  something is already serving port $frontend_port (Live Server?), using it"
elif [ -n "$python_cmd" ]; then
    start_background frontend "$root/frontend" "$python_cmd" -m http.server "$frontend_port" --bind 127.0.0.1
elif command -v npx > /dev/null; then
    start_background frontend "$root/frontend" npx --yes http-server -p "$frontend_port" -a 127.0.0.1 -c-1
else
    echo "python or node is needed to serve the frontend" >&2
    exit 1
fi

# 4. wait until everything answers, then open the site
echo "Waiting for services (the first run downloads dependencies and can take a few minutes)..."
wait_port "$frontend_port" frontend 30
for service in $services; do
    wait_port "$(port_of "$service")" "$service-service"
done

url="http://127.0.0.1:$frontend_port/home/home.html"
echo
echo "Literature Land is running at $url"
echo "Logs are in $log_dir, stop everything with ./stop.sh"
if [ "$no_browser" = false ]; then
    if command -v open > /dev/null; then
        open "$url"
    elif command -v xdg-open > /dev/null; then
        xdg-open "$url" > /dev/null 2>&1 &
    elif command -v cmd.exe > /dev/null; then
        cmd.exe //c start "" "$url"
    fi
fi
