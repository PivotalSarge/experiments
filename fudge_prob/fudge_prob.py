#!/usr/bin/python

import argparse
import collections
import os

counts = {0 : 0}

def addScores(current, remaining):
    if remaining <= 1:
        #print 'current=%d remaining=%d' % (current, remaining)
        counts[current - 1] = counts.setdefault(current - 1, 0) + 1
        counts[current] = counts.setdefault(current, 0) + 1
        counts[current + 1] = counts.setdefault(current + 1, 0) + 1
    else:
        addScores(current - 1, remaining - 1)
        addScores(current, remaining - 1)
        addScores(current + 1, remaining - 1)

parser = argparse.ArgumentParser(description='Build the native client.')
parser.add_argument('number', type=int, default=1, help='number of fudge dice')
args = parser.parse_args()

addScores(0, args.number)

total = 0
for score in counts.iterkeys():
    total += counts[score]

sortedCounts = collections.OrderedDict(sorted(counts.items()))
for score in sortedCounts.iterkeys():
    if 0 <= score:
        padding = ' '
    else:
        padding = ''
    print '%s%0d: %f' % (padding, score, ((1. * sortedCounts[score]) / total))