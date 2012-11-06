BusVis
======

Visualization of the bus system of Konstanz.
As argument to one of the main applications (located in the package infovis or it can be built with maven)
an alternative resource path can be used. Additionally the encoding can be
passed as second argument. The alternative path must contain the following files:

-   *stops.csv*

    A character (';') separated file (csv) containing all stops/stations.
    The first column is the name of a station and the second is the unique
    integer id. The next two columns are the geographic coordinates of the
    station. The last two columns are the coordinates in the schematic svg file
    *abstract.svg*. When a station does not occur in the schematic map, the
    value 'UNKNOWN' is used. Any further columns are ignored.

-   *abstract.svg*

    An optional schematic representation of the transportation network as scalable
    vector graphic (svg). The positions of the stations are encoded in *stops.csv*

-   *lines.csv*

    Defines the colors of the transportation lines. The first column is the
    unique name of the line. The next three columns are the color channels,
    red, green, and blue respectively.

-   *edges.csv*

    Defines all edges. The first column is the name of the line as defined in
    *lines.csv*. The second column is the tour number - subsequent tours must have
    distinct tour numbers. The third column is the start station id. The next
    two columns are the start and end time of the edge in seconds since midnight.
    The last column is the id of the destination station.

-   *walking-dists*

    Optionally defines distances between all stations. The distances are undirected and the
    distance from a station to itself is not needed. The first two columns define
    the pair of stations and the third is the walking time in seconds. 
