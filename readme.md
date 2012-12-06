## BusVis

Visualization of transit systems.
As argument to one of the main applications (located in the package infovis or it can be built with maven)
an alternative resource path can be used.
The alternative path can either be in the internal csv format or a zip file
in the [GTFS][1] (General Transit Feed Specification).
A more detailed explanation of
command line arguments can be obtained by passing `-h` or `--help` on the command line.

### Internal CSV-Format

The internal csv format is automatically used when the path given as first argument
is a directory. This directory must contain the following files:

-   *stops.csv*

    A character (';') separated file (csv) containing all stops/stations.
    The first column is the name of a station and the second is the unique
    integer id. The next two columns are the geographic coordinates of the
    station. The last two columns are the coordinates in the schematic svg file
    *abstract.svg*. When a station does not occur in the schematic map, the
    value 'UNKNOWN' is used. Any further columns are ignored.

-   *abstract.svg* (optional)

    An optional schematic representation of the transportation network as scalable
    vector graphic (svg). The positions of the stations are encoded in *stops.csv*

-   *lines.csv*

    Defines the colors of the transportation lines. The first column is the
    unique name of the line. The next three columns are the color channels,
    red, green, and blue respectively. An optional fourth column has a long
    version of the name of the lines.

-   *edges.csv*

    Defines all edges. The first column is the name of the line as defined in
    *lines.csv*. The second column is the tour number - subsequent tours must have
    distinct tour numbers. The third column is the start station id. The next
    two columns are the start and end time of the edge in seconds since midnight.
    The last column is the id of the destination station.

-   *walking-dists.csv* (optional)

    Optionally defines distances between all stations. The distances are undirected and the
    distance from a station to itself is not needed. The first two columns define
    the pair of stations and the third is the walking time in seconds.

-   *settings.ini* (optional)

    A settings file similar to the one used for the GTFS format below.
    Currently only the *scale* value is implemented.

The default character set for the csv files is CP1252 to be excel compliant -- use
the corresponding command line argument to change the character set.

### GTFS-Format

The [GTFS][1] format is used when the path points to a zip file.
The default character set of the GTFS format (UTF-8) is used except
when overwritten by the corresponding command line argument.
In an optional ini file, that is located in the same folder and with the
same name as the zip, properties regarding the way of interpreting
the GTFS file can be set.
Valid fields are:

-   *cache*
    
    Caches the content of the GTFS file in the internal csv format
    to ensure better startup times. However, this will turn the `today`
    value of the *date* field to the actual date.

-   *date*
    
    Sets the date the data should be loaded for.
    To always use the current date the value `today` can be used.
    Otherwise the GTFS style date format (`YYYYMMDD`) must be used.

-   *scale*

    The scaling factor for geographic coordinates.

A sample GTFS data set can be found at `src/main/resources/nyc/mta_20120701.zip`.

[1]: https://developers.google.com/transit/gtfs/
