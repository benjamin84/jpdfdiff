# jpdfdiff

Silly and simple PDF diff side-by-side viewer which offers

- Simple Pagination
- Page content is scaled down/up to render it into the bounding box

Uses [Apache PDFBox](https://github.com/apache/pdfbox) for rendering PDF files.

## Usage

Build with maven

````
mvn package
````

Run without arguments (opens two file choosers):

````
java -jar pdf-compare-1.0-SNAPSHOT.jar
````

![Tool Screenshot](screenshot.png)

## TortoiseSVN / VCS integration

To enable the tool for viewing changed pdf files in your commit dialog you can configure
a special diff viewer for `.pdf` files:

![TortoiseSVN Settings](tortoisesvn.png)

With other VCS clients there should be a similar way to integrate a special diff tool.