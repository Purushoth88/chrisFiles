#!/bin/bash
set -x
echo "start of a.sh"

echo "a=$a, b=$b, c=$c."

export a=1 c=3
echo "a=$a, b=$b, c=$c."

a=1b
set b=2b
export c=3b
echo "a=$a, b=$b, c=$c."

echo "calling b.sh"
b.sh
echo "a=$a, b=$b, c=$c."

echo "sourcing b.sh"
. b.sh
echo "a=$a, b=$b, c=$c."

echo "end of a.sh"
