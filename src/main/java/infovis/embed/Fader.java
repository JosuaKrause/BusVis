package infovis.embed;

import infovis.data.BusStation;

public interface Fader {

  void setPredict(BusStation station);

  void initialize(BusStation from, long currentTimeMillis, int normal);

  boolean inFade();

}
