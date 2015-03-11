Generated by Brick's Music Studio 1.5
Developed by Guido Truffelli www.aga.it/~guy
/* Special thanks to Brian Bagnall */

class SamplePlayer {
  private static final short [] freq = {
    5167,2, 4866,1, 5426,1, 4995,1, 818,1, 732,1, 43,1, 0,2, 43,3, 559,1,
    689,1, 3962,1, 1808,1, 2024,1, 2067,2, 43,3, 4091,3, 2282,1, 1291,1, 1119,1,
    43,1, 86,1, 559,3, 1593,2, 1808,1, 645,1, 689,1, 43,4, 2153,1, 4005,3,
    2325,1, 43,2, 129,1, 43,1, 2067,1, 1765,1, 1722,1, 5081,1, 1679,2, 86,1,
    43,3, 2497,1, 2583,1, 2670,2, 43,2, 3014,3, 2756,1, 5167,1, 5340,1, 1162,1,
    990,1, 947,1, };

  public static void play() {
    for(int i=0;i<freq.length; i+=2) {
      final short w = freq[i+1];
      Sound.playTone(freq[i], w);
      try { Thread.sleep(w*10); } catch (InterruptedException e) {}
    }
  }
  public static void main(String [] args) { SamplePlayer.play(); }
}