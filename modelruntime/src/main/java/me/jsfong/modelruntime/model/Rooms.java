package me.jsfong.modelruntime.model;
/*
 *
 */


import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Rooms {

  private List<String> dataList = new ArrayList<>();


  public Rooms add(String string) {
    dataList.add(string);
    return this;
  }

}
