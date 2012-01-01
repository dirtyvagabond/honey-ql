# About

HoneyQL is a sweet little SQL for Factual's public API. It comes with a command line interface that lets you run SQL-like query statements against all of Factual's tables.


# Installation

First you will need:

* Java installed on your machine
* A Factual API key and secret

Then download Honey unto your machine and run it like this:
    java -jar honey.jar

# Supported SQL Subset

## SELECT

## WHERE (row filters)

HoneyQL supports a WHERE clause for row filtering.

### Supported filter operations

* =
* !=
* >
* <
* <=
* >=
* is null
* is not null

### LIKE

You can use the LIKE syntax to do "begins with" matches. For example:

    > select name from places where name like 'starbuck%'

You cannot do "ends with" matches with HoneyQL.

### Examples

Find Los Angeles Restaurants having an official "attire required" policy:

    > select name, attire_required from restaurants-us where locality = 'los angeles' and attire_required is not null

View Los Angeles restaurant owners in descending order of their price rating:

    > select name, owner, price from restaurants-us where locality = 'los angeles' and owner is not null order by price desc

## Full Text Search

HoneyQL supports a custom search() syntax for full text searches across whole rows. For example:

    > select name from places search("cafe")

You can also target your full text search to a specific column, using <tt>like</tt> without a wildcard. For example, the following performs a Full Text Search for place names matching "starbucks":

    > select name from places where name like 'starbucks'

# Example Queries

## Simplest possible SELECT statement:

    > select * from places

## Get a random sample of 12 Place names:

    > select name from places limit 12

## Full text search Places for "cafe" in Los Angeles, sort alphabetically by name

    > select name, tel, locality, category from places search('cafe') where locality = 'los angeles' order by name


