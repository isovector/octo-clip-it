from datetime import datetime
import re

class Entry:
  def __init__(self, tup):
    self.ent_type = tup[0]

    if ":" in tup[1]:
      colon = tup[1].find(":")
      self.title = tup[1][:colon]
      self.subtitle = tup[1][colon:].strip()
    else:
      self.title = tup[1]
      self.subtitle = ""

    self.author = tup[2]
    self.location = tup[3]
    self.when = tup[4]
    self.highlight = tup[5]


def parse(entry):
  lines = map(lambda x: x.strip(), entry.split("\n"))

  bookmatch = re.search("(.+)\(([^)]*)\)", lines[0])
  if not bookmatch:
    bookmatch = re.search("(.+)-(.+)", lines[0])
  title = bookmatch.group(1).strip()
  author = bookmatch.group(2).strip()

  metamatch = re.search("- ([A-Za-z]+) (.+)\| Added on (.+)", lines[1])
  entrytype = metamatch.group(1)
  location = metamatch.group(2)
  when = datetime.strptime(metamatch.group(3), "%A, %B %d, %Y, %I:%M %p") 

  highlight = ""
  if len(lines) >= 4:
    highlight = "\n".join(lines[3:])

  return Entry((entrytype, title, author, location, when, highlight))


entries = []
with open("My Clippings.txt") as f:
  for entry in f.read()[3:].split("==========")[:-1]:
    entries.append(parse(entry.strip()))

entries = filter(lambda x: x.ent_type == "Highlight", entries)
books = { }


for entry in entries:
  tup = (entry.title, entry.author)
  if tup not in books:
    books[tup] = []
  books[tup].append(entry)


def title_sort(book):
  title = book[0]
  if title[0:4] == "The ":
    return title[4:]
  return title

print "---"
print "layout: page"
print "title: \"Earnest Money\""
print "date: 2013-12-11 00:16"
print "comments: false"
print "sharing: false"
print "footer: true"
print "---"

print ""
for book in sorted(books.keys(), key=title_sort):
  highlights = books[book]
  print "## %s by %s" % book
  print
  for entry in highlights:
    print "* %s" % entry.highlight
  print 
