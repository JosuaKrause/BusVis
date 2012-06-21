BusVis
======

Visualization of the bus system of Konstanz.
As argument to the main application (located in infovis.Main)
an alternative resource path can be used. This alternative
path must have the following files:

-   *stops.csv*

    A comma (';') separated file (csv) containing all stops/stations.
    The first column is the name of a station and the second is the unique
    integer id. The next two columns are the geographic coordinates of the
    station. The last two columns are the coordinates in the schematic svg file
    *abstract.svg*. When a station does not occur in the schematic map, the
    value 'UNKNOWN' is used. Any further columns are ignored.

-   *abstract.svg*

    A schematic representation of the transportation network as scalable
    vector graphic (svg). The positions of the stations are encoded in *stops.csv*

-   *linecolor.csv*

    Defines the colors of the transportation lines. The first column is the
    unique name of the line. The next three columns are the color channels,
    red, green, and blue respectively.

-   *stopinfo.csv*

    Contains a list of all stops a line visits in a cycle. The first column is
    the unique name of the line. The following columns are the station ids the
    line will take in its course. Stations can be skipped in the schedule. So
    when a line visits stations A, B, D in one route and A, D, C in another the
    ids of the stations must be as follows: 1, 2, 4, 3 With the ids 1 for A, 2
    for B, and so on.

-   *lines*

    Is a folder containing the actual schedules for the declared lines. The
    filenames of the csv files are the unique name of the line with the extension
    '.csv'. A file contains the schedule for the line with the given name. Each
    row represents one cycle. The first column is the id of the bus station. The
    next column is the arrival time and the next column again is the departure
    time. Then the next column is the id of the next bus station and so on. The
    first arrival time is ignored. The end of a cycle is signaled by a '-1' after
    an arrival time. Every following column is ignored. Times are encoded as
    hours and minutes separated by a colon ':'. Hours are in 24 hour format.
