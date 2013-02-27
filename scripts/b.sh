#!/bin/bash
set -x
echo "start of b.sh"

echo "a=$a, b=$b, c=$c."

a=x1
set b=x2
export c=x3
echo "a=$a, b=$b, c=$c."

a=x1b
set b=x2b
export c=x3b
echo "a=$a, b=$b, c=$c."

echo "end of b.sh"
