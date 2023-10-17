# Wikipedia path finder

## Purpose

This program takes a list of problems to solve in a form of input file and returns their solutions to an output file.
The problems this program solves are pathfinding problems. Given two wikipedia articles, this program will return
the shortest paths between them.

## Functionality

This project uses the official Wikimedia API. To find the paths it uses a backtracking algorithm.

Paths to the input and output files are taken from stdin.

Each line of the input file corresponds to a problem. In each line there are three words,
separated by commas. These are respectively a language code, the title of the first article and
the title of the last article.

Each line of the output file corresponds to a solution to one of the problems specified in the input file.
In each line there is at least one word. Multiple words are separated by commas.
Each word corresponds to a wikipedia article. The first article is the article from which we start and the last
article is the one we finish on. The file can contain spaces (' ') only after commas.

The `iofiles` folder contains example input and output files.
Given the example input, the program should take about 210 seconds to evaluate.

## Command line arguments

There can be either

* two  - they have to be positive integers. The first one is interpreted as maximal path length and
  the other as the number of top results to output
* none - backtrack parameters are assigned their default values (3, 3)

## Known bugs

 - ~~When querying links using the Wikimedia API, the API sometimes returns links that are hidden and cannot be
accessed by a wikipedia user without inspecting the webpage's source code.
Thus, some of the paths returned by this program may not conform to the project specification.~~ <br>
The hidden links can be accessed by clicking on buttons and expanding HTML elements.<br>
For example, at https://en.wikipedia.org/wiki/YouTube, even though a simple search (Ctrl+F)
for phrase "Poland" gives no results, one can access the link to Poland's wikipedia article by scrolling 
down to https://en.wikipedia.org/wiki/YouTube#International_and_localization and clicking the
"Countries with YouTube localization" button.
