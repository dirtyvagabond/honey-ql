# About

HoneyQL is a sweet little SQL for Factual's public API. It comes with a command line interface that lets you run SQL-like query statements against Factual's data.

Here's an example of using HoneyQL to find a few restaurant owners offering wifi, in descending order of their price rating, and including their attire:

````
> SELECT name, owner, price, attire \
  FROM restaurants-us \
  WHERE locality = 'los angeles' \
  AND owner IS NOT NULL \
  AND wifi = true \
  ORDER BY price DESC LIMIT 3
````
<pre>
+---------------+--------------+-----+------------+
|name           |owner         |price|attire      |
+---------------+--------------+-----+------------+
|Tower Bar      |Jeff Klein    |5    |smart casual|
+---------------+--------------+-----+------------+
|Asia Restaurant|Bobby Owsinski|3    |null        |
+---------------+--------------+-----+------------+
|Boardners      |Tricia La     |3    |casual      |
+---------------+--------------+-----+------------+
</pre>

# Installation

Make sure you have:

* Java installed on your machine
* A valid Factual API key and secret

[Download Honey 1.0.2](http://github.com/dirtyvagabond/honey-ql/blob/master/bin/honey_1_0_2.jar?raw=true) to your machine...

... and run it like this:

    java -jar honey_1_0_2.jar

# Supported SQL Subset

* SELECT
* COUNT
* FROM
* WHERE
* ORDER BY
* LIMIT
* OFFSET

# HoneyQL Specific Syntax

* SEARCH (Full Text Search)
* NEAR (Geo proximity filter, integrated with Data Science Toolkit)

# SELECT

HoneyQL query statements begin with SELECT. Simple examples:

    > select * from places

    > select name, tel, website from restaurants-us

## Limitations

* The amount of results returned will be limited by Factual's API, based on your account with Factual.
* GROUP BY is not supported.
* HAVING is not supported.

# COUNT

You can use the COUNT clause to get a full row count, just like with traditional SQL. For example:

    > select count(*) from global
    
You can also provide a column name, just like traditional SQL, and the COUNT will count the rows from the results where that column name is not null. For example:

    > select count(website) from places where locality = 'los angeles'

## Limitations

* COUNT does not support any additional syntax, such as DISTINCT.
* COUNT does not support multiple column names (but you can provide a WHERE clause to filter out nulls, like "... [COL_NAME] IS NOT NULL".

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

    > select name from places search('cafe')

You can use SEARCH along with a WHERE clause. For example:

    > select name from places search('cafe') where category like 'Food%'

If you want to do Full Text Search for only a specific column, you use <tt>like</tt> with no wildcards. For example:

    > select name from places where name like 'starbucks'

# Geo Proximity Filter (NEAR)

The NEAR syntax lets you describe a place's address with free text and will attempt to locate that place then find places near to it.

For example, find places near Factual:

    > select name from places near('1801 avenue of the stars, los angeles, ca')

This feature queries the Data Science Toolkit to do the coordinate lookup based on your address term. The query will fail if Data Science Toolkit cannot find a match. This often happens if your address text is unclear or incomplete.

You can use NEAR along with a WHERE clause. For example:

    > select name from restaurants-us near('1801 avenue of the stars, los angeles, ca') where meal_deliver = true and meal_dinner = true

Same as above, plus sort by distance:

    > select name, _distance from restaurants-us near('1801 avenue of the stars, los angeles, ca') where meal_deliver = true and meal_dinner = true order by _distance
