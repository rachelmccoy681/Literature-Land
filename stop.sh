#!/usr/bin/env bash
# stops the services and frontend started by start.sh and shuts down kafka
# usage: ./stop.sh
#        ./stop.sh --keep-kafka

keep_kafka=false
[ "$1" = "--keep-kafka" ] && keep_kafka=true

root="$(cd "$(dirname "$0")" && pwd)"
pid_file="$root/.run/pids.txt"

if [ -f "$pid_file" ]; then
    echo "Stopping services and frontend..."
    while read -r pid; do
        [ -z "$pid" ] && continue
        if [ -f "/proc/$pid/winpid" ] && command -v taskkill > /dev/null; then
            # git bash on windows: java is a native child, so stop the whole windows process tree
            taskkill //T //F //PID "$(cat "/proc/$pid/winpid")" > /dev/null 2>&1
        fi
        # each job runs in its own process group, so this also stops the java process mvnw started
        kill -- "-$pid" 2>/dev/null || kill "$pid" 2>/dev/null
    done < "$pid_file"
    rm -f "$pid_file"
else
    echo "No services were started by start.sh"
fi

if [ "$keep_kafka" = false ] && command -v docker > /dev/null; then
    echo "Stopping Kafka..."
    docker compose -f "$root/docker-compose.yml" down
fi
