#!/usr/bin/python

import argparse
import collections
import os

by_roll = {}

def make_key(dice):
    key = ''
    for die in sorted(dice):
        key += die
    return key

def score_key(key):
    score = 0
    dice = list(str(key))
    for die in dice:
        if die == '+':
            score += 1
        elif die == '-':
            score -= 1
    return score

def recurse(target, current):
    if target <= len(current):
        key = make_key(current)
        by_roll[key] = by_roll.setdefault(key, 0) + 1
    else:
        plus = list(current)
        plus.append('+')
        recurse(target, plus)
        zero = list(current)
        zero.append('0')
        recurse(target, zero)
        minus = list(current)
        minus.append('-')
        recurse(target, minus)

def print_probabilities(counts):
    total = 0
    for key in counts.iterkeys():
        total += counts[key]

    sortedCounts = collections.OrderedDict(sorted(counts.items()))
    for key in sortedCounts.iterkeys():
        print '%s: %d/%d (%f)' % (str(key).rjust(5, ' '), sortedCounts[key], total, ((1. * sortedCounts[key]) / total))

parser = argparse.ArgumentParser(description='Calculate probabilities of fudge dice.')
parser.add_argument('number', type=int, default=1, help='number of fudge dice')
args = parser.parse_args()

recurse(args.number, [])

print 'By roll:'
print_probabilities(by_roll)

print ''
print 'By score:'
by_score = {}
for roll in by_roll.iterkeys():
    roll = str(roll)
    score = score_key(roll)
    # print '%s -> %d' % (roll, score)
    by_score[score] = by_score.setdefault(score, 0) + by_roll[roll]
    #print 'by_score[%s]=%d' % (score, by_score[score])

print_probabilities(by_score)
