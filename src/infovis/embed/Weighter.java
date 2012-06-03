package infovis.embed;

public interface Weighter {

  double weight(SpringNode from, SpringNode to);

  boolean hasWeight(SpringNode from, SpringNode to);

  Iterable<SpringNode> nodes();

  double springConstant();

}
