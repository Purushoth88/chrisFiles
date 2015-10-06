#!/bin/bash

function print_git_object()
{
  id=$1
  prefix=$2
  type=$3
  [ -z "$type" ] && type=$(git cat-file -t $id)
  case $type in
  tree)
    git cat-file -p $id | while read memberMode memberType memberId memberName ;do
      case $memberType in
      blob)
        echo "$prefix$memberName ${memberId:0:7} / $memberMode"
        ;;
      tree)
        echo "$prefix$memberName/ ${memberId:0:7}"
        print_git_object $memberId "$prefix  " tree
        ;;
      esac
    done
    ;;
  commit)
    echo "commit ${id:0:7}"
    git cat-file -p $id | grep '^parent' | cut -f2 -d' ' | while read parentId ;do echo "$prefix  parent ${parentId:0:7}" ;done
    treeId=$(git cat-file -p $id | grep '^tree' | cut -f2 -d' ')
    echo "$prefix  tree: ${treeId:0:7}"
    print_git_object $treeId "$prefix    " tree
    ;;
  esac
}

if [ "$1" == "-" ] ;then
  while read line ;do print_git_object $line ;done
else
  while [ ! -z "$1" ] ;do
    print_git_object $(git rev-parse $1)
    shift 1
  done
fi
