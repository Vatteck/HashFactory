#!/bin/bash
echo "--- CPU & RAM ---"
top -bn1 | grep "Cpu(s)" | awk '{print "CPU Usage: " $2 "%"}'
free -h | awk '/^Mem:/ {print "RAM Usage: " $3 "/" $2}'
echo "--- GPU (RTX 3060 Laptop) ---"
nvidia-smi --query-gpu=utilization.gpu,memory.used,memory.total --format=csv,noheader,nounits | awk -F', ' '{print "GPU Load: "$1"%, VRAM: "$2"MB/"$3"MB"}'
