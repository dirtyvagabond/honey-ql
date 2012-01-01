# About

HoneyQL is a sweet little SQL for Factual's public API. It comes with a command line interface that lets you run SQL-like query statements against all of Factual's tables.


# Installation

First you will need:

* Java installed on your machine
* A Factual API key and secret

Then download Honey unto your machine and run it like this:
    java -jar honey.jar

# Supported SQL Subset

* SELECT
* FROM
* WHERE
* LIMIT
* OFFSET
* ORDER BY

# HoneyQL Specific Syntax

* SEARCH (Full Text Search)
* NEAR (Geo proximity filter, integrated with Data Science Toolkit)

# SELECT

HoneyQL query statements begin with SELECT. Simple examples:

    > select * from places

    > select name, tel, website from restaurants-us

# WHERE (row filters)

HoneyQL supports a WHERE clause for row filtering.

## Supported filter operations

* =
* !=
* >
* <
* <=
* >=
* like
* in
* not in
* is null
* is not null

## LIKE

You can use the LIKE syntax to do "begins with" matches. For example:

    > select name from places where name like 'starbuck%'

You cannot do "ends with" matches with HoneyQL.

If you surround your term with %, or use no %s, it's treated like a column-specific Full Text Search.

## IN and NOT IN

You can use IN and NOT IN to row filter with lists. For example:

See Los Angeles restaurants founded recently:

    > select name, founded from restaurants-us where locality = 'Los Angeles' and founded in (2010, 2011, 2012)

See Los Angeles restaurants that won't make me dress too nice:

    > select name, attire from restaurants-us where locality = 'Los Angeles' and attire not in ('formal', 'smart casual', 'business casual')

## More Examples of row filtering

Find Los Angeles restaurants having an official "attire required" policy:

    > select name, attire_required from restaurants-us where locality = 'los angeles' and attire_required is not null

View Los Angeles restaurant owners in descending order of their price rating:

    > select name, owner, price from restaurants-us where locality = 'los angeles' and owner is not null order by price desc

View business in Chiang Mai, Thailand that are operational and have a telephone number:

    > select name, tel from global where country = 'TH' and region = 'Chiang Mai' and status = 1 and tel is not null

## Nested logic

You can nest your row filter logic just like you can with SQL, including the use of (), AND, and OR. For example:

Find Los Angeles restaurants that have wifi or smoking:

    > select name, wifi, smoking from restaurants-us where locality = 'Los Angeles' and (wifi = true or smoking = true) 

# Full Text Search (SEARCH)

HoneyQL supports a custom search() syntax for full text searches across whole rows. For example:

    > select name from places search("cafe")

You can also target your full text search to a specific column, using <tt>like</tt> without a wildcard. For example, the following performs a Full Text Search for place names matching "starbucks":

    > select name from places where name like 'starbucks'

# Geo Proximity Filter (NEAR)

The NEAR syntax lets you describe a place's address with free text and will attempt to locate that place then find places near to it.

For example, find places near Factual:

    > select name from places near('1801 avenue of the starts, los angeles, ca')

This feature queries the Data Science Toolkit to do the coordinate lookup based on your address term. The query will fail if Data Science Toolkit cannot find a match. This often happens if your address text is unclear or incomplete.

The NEAR filter can be used along with WHERE. For example, find restaurants near Factual that deliver dinner:

    > select name from restaurants-us near('1801 avenue of the starts, los angeles, ca') where meal_deliver = true and meal_dinner = true

Same as above, plus sort by distance:

    > select name, _distance from restaurants-us near('1801 avenue of the starts, los angeles, ca') where meal_deliver = true and meal_dinner = true order by _distance

# More Example Queries

## Simplest possible SELECT statement:

    > select * from places

## Get a random sample of 12 Place names:

    > select name from places limit 12

## Full text search Places for "cafe" in Los Angeles, sort alphabetically by name

    > select name, tel, locality, category from places search('cafe') where locality = 'los angeles' order by name


