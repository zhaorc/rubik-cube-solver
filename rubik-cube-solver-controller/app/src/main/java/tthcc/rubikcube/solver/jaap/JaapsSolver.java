package tthcc.rubikcube.solver.jaap;

import android.util.Log;

/**
 * 魔方还原算法。<br>
 * 移植自<br>
 * <a href=
 * "http://tomas.rokicki.com/cubecontest/winners.html">http://tomas.rokicki.com/cubecontest/winners.html</href><br>
 * 的第三名<br>
 * <a href=
 * "http://tomas.rokicki.com/cubecontest/jaap.zip">http://tomas.rokicki.com/cubecontest/jaap.zip</href><br>
 * 作者：Jaap Scherphuis of Delft, the Netherlands
 * 原程序是C语言编写。
 *
 * @author tthcc 2018年9月8日 上午9:40:03
 */
public class JaapsSolver {
    private static final String TAG = JaapsSolver.class.getSimpleName();
    private static final String SEQUANCE   = "FBRLUD";
    // RLFBUD is the face order used for input, so that a correctly oriented
    // piece in the input has its 'highest value' facelet first. The rest of the
    // program uses moves in FBRLUD order.
    private static final String FACES      = "RLFBUD";
    // I use char arrays here cause they can be initialised with a string
    // which is shorter than initialising other arrays.
    // Internally cube uses slightly different ordering to the input so that
    //  orbits of stage 4 are contiguous. Note also that the two corner orbits
    //  are diametrically opposite each other.
    //input:  UF UR UB UL  DF DR DB DL  FR FL BR BL  UFR URB UBL ULF   DRF DFL DLB DBR
    //        A  B  C  D   E  F  G  H   I  J  K  L   M   N   O   P     Q   R   S   T
    //        A  E  C  G   B  F  D  H   I  J  K  L   M   S   N   T     R   O   Q   P
    //intrnl: UF DF UB DB  UR DR UL DL  FR FL BR BL  UFR UBL DFL DBR   DLB DRF URB ULF
    private char[]              order      = "AECGBFDHIJKLMSNTROQP".toCharArray();
    //To quickly recognise the pieces, I construct an integer by setting a bit for each
    // facelet. The unique result is then found on the list below to map it to the correct
    // cubelet of the cube.
    //intrnl: UF DF UB DB  UR DR UL DL  FR FL BR BL  UFR UBL DFL DBR   DLB DRF URB ULF
    //bithash:20,36,24,40, 17,33,18,34, 5, 6, 9, 10, 21, 26, 38, 41,   42, 37, 25, 22
    private char[]              bithash    = "TdXhQaRbEFIJUZfijeYV".toCharArray();
    //Each move consists of two 4-cycles. This string contains these in FBRLUD order.
    //intrnl: UF DF UB DB  UR DR UL DL  FR FL BR BL  UFR UBL DFL DBR   DLB DRF URB ULF
    //        A  B  C  D   E  F  G  H   I  J  K  L   M   N   O   P     Q   R   S   T
    private char[]              perm       = "AIBJTMROCLDKSNQPEKFIMSPRGJHLNTOQAGCEMTNSBFDHORPQ".toCharArray();

    // current cube position
    private char[]              pos        = new char[20];
    private char[]              ori        = new char[20];
    private char[]              val        = new char[20];

    // pruning tables, 2 for each phase
    private char[][]            tables     = new char[8][];
    // current phase solution`
    private int[]               move       = new int[20];
    private int[]               moveamount = new int[20];
    // current phase being searched (0,2,4,6 for phases 1 to 4)
    private int                 phase      = 0;
    // Length of pruning tables. (one dummy in phase 1);
    private static final int[]               tablesize  = { 1, 4096, 6561, 4096, 256, 1536, 13824, 576 };

    private static final int    CHAROFFSET = 65;

    public static void main(String[] args) {
//        String sequance = "B2 F+ U+ B2 U- B2 U+ F2 D- U- R- D- R2 D+ R+ B2 R- D- L+ U- L- U- B- R- F2";
//        String input = "DF RD DL BR FU LF BL FR UB RU LU BD FLD RBU LUB ULF DRF BDL RDB FRU";
//        JaapsSolver solver = new JaapsSolver();
//        String moves = solver.solve(input);
//        //XXX
//        System.out.println(moves);
//        moves = moves.replaceAll("3", "'").replaceAll("1", "");
//        //XXX
//        String clearInput = sequance.replaceAll(" ", "").replaceAll("-", "'").replaceAll("\\+", "");
//        System.out.println("input:" + String.format("%04d%s", clearInput.length(), clearInput));
//        System.out.println("moves:" + String.format("%04d%s", moves.length(), moves));
    }

    /**
     *
     * @param facelets, 顺序为：<br>
     *                  URFDLB
     *                  <pre>
     *                  The names of the facelet positions of the cube
     *                  |************|
     *                  |*U1**U2**U3*|
     *                  |************|
     *                  |*U4**U5**U6*|
     *                  |************|
     *                  |*U7**U8**U9*|
     *                  |************|
     *      ************|************|************|************|
     *      *L1**L2**L3*|*F1**F2**F3*|*R1**R2**F3*|*B1**B2**B3*|
     *      ************|************|************|************|
     *      *L4**L5**L6*|*F4**F5**F6*|*R4**R5**R6*|*B4**B5**B6*|
     *      ************|************|************|************|
     *      *L7**L8**L9*|*F7**F8**F9*|*R7**R8**R9*|*B7**B8**B9*|
     *      ************|************|************|************|
     *                  |************|
     *                  |*D1**D2**D3*|
     *                  |************|
     *                  |*D4**D5**D6*|
     *                  |************|
     *                  |*D7**D8**D9*|
     *                  |************|
     *                  </pre>
     * @return
     */
    public String solveByFacelets(String facelets) {
        // UF UR UB UL DF DR DB DL FR FL BR BL UFR URB UBL ULF DRF DFL DLB DBR
        // URFDLB
        String U = facelets.substring(0, 9);
        String R = facelets.substring(9, 18);
        String F = facelets.substring(18, 27);
        String D = facelets.substring(27, 36);
        String L = facelets.substring(36, 45);
        String B = facelets.substring(45, 54);

        String UF = "" + U.charAt(7) + F.charAt(1);
        String UR = "" + U.charAt(5) + R.charAt(1);
        String UB = "" + U.charAt(1) + B.charAt(1);
        String UL = "" + U.charAt(3) + L.charAt(1);

        String DF = "" + D.charAt(1) + F.charAt(7);
        String DR = "" + D.charAt(5) + R.charAt(7);
        String DB = "" + D.charAt(7) + B.charAt(7);
        String DL = "" + D.charAt(3) + L.charAt(7);

        String FR = "" + F.charAt(5) + R.charAt(3);
        String FL = "" + F.charAt(3) + L.charAt(5);

        String BR = "" + B.charAt(3) + R.charAt(5);
        String BL = "" + B.charAt(5) + L.charAt(3);

        String UFR = "" + U.charAt(8) + F.charAt(2) + R.charAt(0);
        String URB = "" + U.charAt(2) + R.charAt(2) + B.charAt(0);
        String UBL = "" + U.charAt(0) + B.charAt(2) + L.charAt(0);
        String ULF = "" + U.charAt(6) + L.charAt(2) + F.charAt(0);

        String DRF = "" + D.charAt(2) + R.charAt(6) + F.charAt(8);
        String DFL = "" + D.charAt(0) + F.charAt(6) + L.charAt(8);
        String DLB = "" + D.charAt(6) + L.charAt(6) + B.charAt(8);
        String DBR = "" + D.charAt(8) + B.charAt(6) + R.charAt(8);

        String input = String.format("%s %s %s %s %s %s %s %s %s %s %s %s %s %s %s %s %s %s %s %s",
                UF, UR, UB, UL, DF, DR, DB, DL, FR, FL, BR, BL, UFR, URB, UBL, ULF, DRF, DFL, DLB, DBR);
        //XXX
        Log.i(TAG, ">>>input=" + input);
        String output = this.solve(input);
        return output;
    }

    /**
     * @param inputString 格式为：<br>
     *                     UF UR UB UL  DF DR DB DL  FR FL BR BL  UFR URB UBL ULF   DRF DFL DLB DBR
     * @return
     */
    public String solve(String inputString) {
        int f, i = 0, j = 0, k = 0, pc, mor;

        // initialise tables
        for (; k < 20; k++) {
            val[k] = (char) (k < 12 ? 2 : 3);
        }
        for (; j < 8; j++) {
            filltable(j);
        }

        // read input, 20 pieces worth
        String[] inputs = inputString.split(" ");

        for (; i < 20; i++) {
            char[] input = inputs[i].toCharArray();
            f = pc = k = mor = 0;
            for (; f < val[i]; f++) {
                // read input from stdin, or...
                //     do{cin>>c;}while(c==' ');
                //     j=strchr(faces,c)-faces;
                // ...from command line and get face number of facelet
                //j=strchr(faces,argv[i+1][f])-faces;
                j = FACES.indexOf(input[f]);
                // keep track of principal facelet for orientation
                if (j > k) {
                    k = j;
                    mor = f;
                }
                //construct bit hash code
                pc += 1 << j;
            }
            // find which cubelet it belongs, i.e. the label for this piece
            for (f = 0; f < 20; f++) {
                if (pc == bithash[f] - 64) {
                    break;
                }
            }
            // store piece
            pos[order[i] - CHAROFFSET] = (char) f;
            ori[order[i] - CHAROFFSET] = (char) (mor % val[i]);
        }

        //solve the cube
        // four phases
        StringBuffer moves = new StringBuffer();
        for (; phase < 8; phase += 2) {
            // try each depth till solved
            for (j = 0; !searchphase(j, 0, 9); j++) {

            }
            //output result of this phase
            for (i = 0; i < j; i++) {
                //cout<<"FBRLUD"[move[i]]<<moveamount[i];
                moves.append(SEQUANCE.charAt(move[i])).append(moveamount[i]);
                //cout<<" ";
            }
        }
        return moves.toString().replaceAll("3", "'").replaceAll("1", "");
    }

    // Pruned tree search. recursive.
    private boolean searchphase(int movesleft, int movesdone, int lastmove) {
        // prune - position must still be solvable in the remaining moves available
        if (tables[phase][getposition(phase)] - 1 > movesleft || tables[phase + 1][getposition(phase + 1)] - 1 > movesleft)
            return false;

        // If no moves left to do, we have solved this phase
        if (movesleft == 0)
            return true;

        // not solved. try each face move
        for (int i = 6; i-- > 0;) {
            // do not repeat same face, nor do opposite after DLB.
            if ((i - lastmove) != 0 && ((i - lastmove + 1) != 0 || (i | 1) != 0)) {
                move[movesdone] = i;
                // try 1,2,3 quarter turns of that face
                for (int j = 0; ++j < 4;) {
                    //do move and remember it
                    domove(i);
                    moveamount[movesdone] = j;
                    //Check if phase only allows half moves of this face
                    if ((j == 2 || i >= phase) && searchphase(movesleft - 1, movesdone + 1, i)) {
                        return true;
                    }
                }
                // put face back to original position.
                domove(i);
            }
        }
        // no solution found
        return false;
    }

    // calculate a pruning table
    private void filltable(int ti) {
        int n = 1, l = 1, tl = tablesize[ti];
        // alocate table memory
        tables[ti] = new char[tl];
        //clear table
        //memset(tb, 0, tl);
        for (int i = 0; i < tl; i++) {
            tables[ti][i] = 0;
        }
        //mark solved position as depth 1
        reset();
        tables[ti][getposition(ti)] = 1;

        // while there are positions of depth l
        while (n != 0) {
            n = 0;
            // find each position of depth l
            for (int i = 0; i < tl; i++) {
                if (tables[ti][i] == l) {
                    //construct that cube position
                    setposition(ti, i);
                    // try each face any amount
                    for (int f = 0; f < 6; f++) {
                        for (int q = 1; q < 4; q++) {
                            domove(f);
                            // get resulting position
                            int r = getposition(ti);
                            // if move as allowed in that phase, and position is a new one
                            if ((q == 2 || f >= (ti & 6)) && tables[ti][r] == 0) {
                                // mark that position as depth l+1
                                tables[ti][r] = (char) (l + 1);
                                n++;
                            }
                        }
                        domove(f);
                    }
                }
            }
            l++;
        }
    }

    // set cube to solved position
    private void reset() {
        //        for (int i = 0; i < 20; pos[i] = (char) i, ori[i++] = 0);
        for (int i = 0; i < 20; i++) {
            pos[i] = (char) i;
            ori[i] = 0;
        }
    }

    // sets cube to any position which has index n in table t
    void setposition(int t, int n) {
        int i = 0, j = 12, k = 0;
        char[] corn = "QRSTQRTSQSRTQTRSQSTRQTSR".toCharArray();
        reset();
        switch (t) {
            // case 0 does nothing so leaves cube solved
            case 1://edgeflip
                for (; i < 12; i++, n >>= 1) {
                    ori[i] = (char) (n & 1);
                }
                break;
            case 2://cornertwist
                for (i = 12; i < 20; i++, n /= 3) {
                    ori[i] = (char) (n % 3);
                }
                break;
            case 3://middle edge choice
                for (; i < 12; i++, n >>= 1) {
                    pos[i] = (char) (8 * n & 8);
                }
                break;
            case 4://ud slice choice
                for (; i < 8; i++, n >>= 1) {
                    pos[i] = (char) (4 * n & 4);
                }
                break;
            case 5://tetrad choice,parity,twist
                //corn += n % 6 * 4;
                int start = n % 6 * 4;
                n /= 6;
                for (; i < 8; i++, n >>= 1) {
                    pos[i + 12] = (n & 1) != 0 ? (char) (corn[start + k++] - CHAROFFSET) : (char) j++;
                }
                break;
            case 6://slice permutations
                numtoperm(pos, n % 24, 12);
                n /= 24;
                numtoperm(pos, n % 24, 4);
                n /= 24;
                numtoperm(pos, n, 0);
                break;
            case 7://corner permutations
                numtoperm(pos, n / 24, 8);
                numtoperm(pos, n % 24, 16);
                break;
        }
    }

    // get index of cube position from table t
    private int getposition(int t) {
        int i = -1, n = 0;
        switch (t) {
            // case 0 does nothing so returns 0
            case 1://edgeflip
                // 12 bits, set bit if edge is flipped
                for (; ++i < 12;)
                    n += ori[i] << i;
                break;
            case 2://cornertwist
                // get base 3 number of 8 digits - each digit is corner twist
                for (i = 20; --i > 11;)
                    n = n * 3 + ori[i];
                break;
            case 3://middle edge choice
                // 12 bits, set bit if edge belongs in Um middle slice
                //for(;++i<12;) {n+= (pos[i]&8)?(1<<i):0;
                for (; ++i < 12;) {
                    n += (pos[i] & 8) != 0 ? (1 << i) : 0;
                }
                break;
            case 4://ud slice choice
                // 8 bits, set bit if UD edge belongs in Fm middle slice
                //for(;++i<8;) n+= (pos[i]&4)?(1<<i):0;
                for (; ++i < 8;) {
                    n += (pos[i] & 4) != 0 ? (1 << i) : 0;
                }
                break;
            case 5://tetrad choice, twist and parity
                //int corn[8],j,k,l,corn2[4];
                int[] corn = new int[8], corn2 = new int[4];
                int j, k, l;
                // 8 bits, set bit if corner belongs in second tetrad.
                // also separate pieces for twist/parity determination
                k = j = 0;
                for (; ++i < 8;) {
                    //if ((l = pos[i + 12] - 12) & 4) {
                    l = pos[i + 12] - 12;
                    if ((l & 4) != 0) {
                        corn[l] = k++;
                        n += 1 << i;
                    } else {
                        corn[j++] = l;
                    }
                }
                //Find permutation of second tetrad after solving first
                for (i = 0; i < 4; i++) {
                    corn2[i] = corn[4 + corn[i]];
                }
                //Solve one piece of second tetrad
                for (; --i != 0;) {
                    corn2[i] ^= corn2[0];
                }
                // encode parity/tetrad twist
                n = n * 6 + corn2[1] * 2 - 2;
                if (corn2[3] < corn2[2]) {
                    n++;
                }
                break;
            case 6://two edge and one corner orbit, permutation
                n = permtonum(pos, 0) * 576 + permtonum(pos, 4) * 24 + permtonum(pos, 12);
                break;
            case 7://one edge and one corner orbit, permutation
                n = permtonum(pos, 8) * 24 + permtonum(pos, 16);
                break;
        }
        return n;

    }

    //do a clockwise quarter turn cube move
    private void domove(int m) {
        //char *p=perm+8*m, i=8;
        int i = 8;
        //cycle the edges
        //cycle(pos, p);
        cycle(pos, 0, perm, 8 * m);
        //cycle(ori, p);
        cycle(ori, 0, perm, 8 * m);
        //cycle the corners
        //cycle(pos, p + 4);
        cycle(pos, 0, perm, 8 * m + 4);
        //cycle(ori, p + 4);
        cycle(ori, 0, perm, 8 * m + 4);
        //twist corners if RLFB
        if (m < 4)
            for (; --i > 3;) {
                //twist(p[i], i & 1);
                twist(perm[8 * m + i], i & 1);
            }
        //flip edges if FB
        if (m < 2) {
            for (i = 4; i-- != 0;) {
                //twist(p[i], 0);
                twist(perm[8 * m + i], 0);
            }
        }
    }

    // convert permutation of 4 chars to a number in range 0..23
    private int permtonum(char[] p, int offset) {
        int n = 0;
        for (int a = 0; a < 4; a++) {
            n *= 4 - a;
            for (int b = a; ++b < 4;) {
                if (p[b + offset] < p[a + offset]) {
                    n++;
                }
            }
        }
        return n;
    }

    // convert number in range 0..23 to permutation of 4 chars.
    private void numtoperm(char[] p, int n, int o) {
        //p += o;
        p[3 + o] = (char) o;
        for (int a = 3; a-- != 0;) {
            p[a + o] = (char) (n % (4 - a) + o);
            n /= 4 - a;
            for (int b = a; ++b < 4;) {
                if (p[b + o] >= p[a + o]) {
                    p[b + o]++;
                }
            }
        }
    }

    // Cycles 4 pieces in array p, the piece indices given by a[0..3].
    private void cycle(char[] p, int pOffset, char[] a, int aOffset) {
        //SWAP(p[*a-CHAROFFSET],p[a[1]-CHAROFFSET]);
        char temp = p[pOffset + a[aOffset] - CHAROFFSET];
        p[pOffset + a[aOffset] - CHAROFFSET] = p[pOffset + a[1 + aOffset] - CHAROFFSET];
        p[pOffset + a[1 + aOffset] - CHAROFFSET] = temp;
        //SWAP(p[*a-CHAROFFSET],p[a[2]-CHAROFFSET]);
        temp = p[pOffset + a[aOffset] - CHAROFFSET];
        p[pOffset + a[aOffset] - CHAROFFSET] = p[pOffset + a[2 + aOffset] - CHAROFFSET];
        p[pOffset + a[2 + aOffset] - CHAROFFSET] = temp;
        //SWAP(p[*a-CHAROFFSET],p[a[3]-CHAROFFSET]);
        temp = p[pOffset + a[aOffset] - CHAROFFSET];
        p[pOffset + a[aOffset] - CHAROFFSET] = p[pOffset + a[3 + aOffset] - CHAROFFSET];
        p[pOffset + a[3 + aOffset] - CHAROFFSET] = temp;
    }

    // twists i-th piece a+1 times.
    private void twist(int i, int a) {
        i -= CHAROFFSET;
        ori[i] = (char) ((ori[i] + a + 1) % val[i]);
    }
}
