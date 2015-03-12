# Introduction #
KML (keynote markup language) is the XML-type document tree that stores the coordinates and such from Google SketchUp. The KML documents are all stored in a compressed KMZ file.

**NOTE**: When saving your element, please give it a different name for each game element as SketchUp, otherwise the each KML file will be put into a single KMZ.

The information that will be needed for creating an element/object from the KML files are found in the following locations:

**UPDATE 4/20:** Style tags have now been implemented (at least partially). The style tags are found in the top level of the kml, and are referenced in the geometry using the styleUrl tag.

For details, it's best to (try to) parse the code or to look at the kml reference at [http://earth.google.com/kml/kml\_tags\_21.html](http://earth.google.com/kml/kml_tags_21.html)

kml


--->Document

--->--->name

--->--->DocumentOrigin

--->--->--->coordinates

--->Folder

--->--->--->Placemark

--->--->--->--->GeometeryCollection

--->--->--->--->--->Polygon // faces

--->--->--->--->--->--->outerBoundaryIs

--->--->--->--->--->--->--->LinearRing

--->--->--->--->--->--->--->--->coordinates

--->--->--->--->--->LineString // edges

--->--->--->--->--->--->coordinates