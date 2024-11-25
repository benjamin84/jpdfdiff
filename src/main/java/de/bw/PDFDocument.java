/*
 * Copyright (c) 2024. benjamin.wilger@gmail.com
 */

package de.bw;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;

import org.apache.pdfbox.pdmodel.PDDocument;

final class PDFDocument implements Closeable
{
   private final File file;
   private final PDDocument document;
   private final Side side;

   public PDFDocument (File file, PDDocument document, Side side)
   {
      this.file = file;
      this.document = document;
      this.side = side;
   }

   @Override
   public void close () throws IOException
   {
      document.close ();
   }

   public PDDocument getDocument ()
   {
      return document;
   }

   public File getFile ()
   {
      return file;
   }

   public Side getSide ()
   {
      return side;
   }
}
