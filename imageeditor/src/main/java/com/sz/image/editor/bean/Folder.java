package com.sz.image.editor.bean;

import java.util.List;

/**
 * 폴더정보를 담고있는 객체.
 */
public class Folder {
  public String name;
  public String path;
  public Image cover;
  public List<Image> images;

  @Override
  public boolean equals(Object o) {
    try {
      Folder other = (Folder) o;
      return this.path.equalsIgnoreCase(other.path);
    } catch (ClassCastException e) {
      e.printStackTrace();
    }
    return super.equals(o);
  }
}
