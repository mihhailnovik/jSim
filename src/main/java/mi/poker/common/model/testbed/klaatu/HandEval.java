package mi.poker.common.model.testbed.klaatu;

//2008Apr27.0
//fix hand6Eval's calls to flushAndOrStraight6
//2006Dec05.0
//original Java release, ported from C

public final class HandEval {

private HandEval() {}   // no instances

/**
* Returns a value which can be used in building a parameter to one of the HandEval evaluation methods.
* @param card a {@link Card}
* @return a value which may be bitwise OR'ed or added to other such
* values to build a parameter to one of the HandEval evaluation methods.
*/
public static long encode(final Card card) {
return 0x1L << (card.suitOf().ordinal()*13 + card.rankOf().ordinal());
}

/**
* Returns a value which can be used as a parameter to one of the HandEval evaluation methods.
* @param cs a {@link CardSet}
* @return a value which can be used as a parameter to one of the HandEval evaluation methods.
* The value may also be bitwise OR'ed or added to other such
* values to build an evaluation method parameter.
*/
public static long encode(final CardSet cs) {
long result = 0;
for (Card c : cs)
    result |= encode(c);
return result;
}

public static enum HandCategory { NO_PAIR, PAIR, TWO_PAIR, THREE_OF_A_KIND, STRAIGHT,
                    FLUSH, FULL_HOUSE, FOUR_OF_A_KIND, STRAIGHT_FLUSH; }

private static final int   BOT_SHIFT    = 16;
private static final int   TOP_SHIFT    = BOT_SHIFT + 4;
private static final int   VALUE_SHIFT  = TOP_SHIFT + 4;

//javac doesn't propagate NO_PAIR (==0) so doesn't constant-fold it out of bitwise-or expressions
//private static final int   NO_PAIR          = HandCategory.NO_PAIR.ordinal() << VALUE_SHIFT;
private static final int   PAIR             = HandCategory.PAIR.ordinal() << VALUE_SHIFT;
private static final int   TWO_PAIR         = HandCategory.TWO_PAIR.ordinal() << VALUE_SHIFT;
private static final int   THREE_OF_A_KIND  = HandCategory.THREE_OF_A_KIND.ordinal() << VALUE_SHIFT;
private static final int   STRAIGHT         = HandCategory.STRAIGHT.ordinal() << VALUE_SHIFT;
private static final int   FLUSH            = HandCategory.FLUSH.ordinal() << VALUE_SHIFT;
private static final int   FULL_HOUSE       = HandCategory.FULL_HOUSE.ordinal() << VALUE_SHIFT;
private static final int   FOUR_OF_A_KIND   = HandCategory.FOUR_OF_A_KIND.ordinal() << VALUE_SHIFT;
private static final int   STRAIGHT_FLUSH   = HandCategory.STRAIGHT_FLUSH.ordinal() << VALUE_SHIFT;

/* Arrays for which index is bit mask of card ranks in hand: */
private static final int   ARRAY_SIZE       = 0x1FC0 + 1;           // all combos of up to 7 of LS 13 bits on

private static final int[] straightValue    = new int[ARRAY_SIZE];  // STRAIGHT | (straight's high card rank (5..14) << BOT_SHIFT); 0 if no straight
private static final int[] nbrOfRanks       = new int[ARRAY_SIZE];  // count of bits set
private static final int[] hiTopRankTWO_PAIR = new int[ARRAY_SIZE]; // TWO_PAIR | ((rank (2..kA) of the highest bit set) << TOP_SHIFT)
private static final int[] hiBotRank        = new int[ARRAY_SIZE];  // (rank (2..kA) of the highest bit set) << BOT_SHIFT
private static final int[] hiRankMask       = new int[ARRAY_SIZE];  // all bits except highest reset
private static final int[] hi2RanksMask     = new int[ARRAY_SIZE];  // all bits except highest 2 reset
private static final int[] hi3RanksMask     = new int[ARRAY_SIZE];  // all bits except highest 3 reset
private static final int[] hi5RanksMask     = new int[ARRAY_SIZE];  // all bits except highest 5 reset

private static final int[] lo5RanksMask     = new int[ARRAY_SIZE]; // all bits except lowest 5 8-or-better reset; 0 if not at least 5 8-or-better bits set
private static final int[] lo3RanksMask     = new int[ARRAY_SIZE]; // all bits except lowest 3 8-or-better reset; 0 if not at least 3 8-or-better bits set

/**
*  Greater than any return value of the HandEval evaluation methods.
*/
public static final int NO_8_LOW = STRAIGHT_FLUSH + (1 << VALUE_SHIFT);

private static final int[] loEvalOrNo8Low   = new int[ARRAY_SIZE]; // 5 bits set in LS 8 bits, or NO_8_LOW */

private static int flushAndOrStraight7(final int ranks, final int c, final int d, final int h, final int s) {

int i, j;

if ((j = nbrOfRanks[c]) > 7 - 5) {
    // there's either a club flush or no flush
    if (j >= 5)
        if ((i = straightValue[c]) == 0)
            return FLUSH | hi5RanksMask[c];
        else
            return (STRAIGHT_FLUSH - STRAIGHT) + i;
} else if ((j += (i = nbrOfRanks[d])) > 7 - 5) {
    if (i >= 5)
        if ((i = straightValue[d]) == 0)
            return FLUSH | hi5RanksMask[d];
        else
            return (STRAIGHT_FLUSH - STRAIGHT) + i;
} else if ((j += (i = nbrOfRanks[h])) > 7 - 5) {
    if (i >= 5)
        if ((i = straightValue[h]) == 0)
            return FLUSH | hi5RanksMask[h];
        else
            return (STRAIGHT_FLUSH - STRAIGHT) + i;
} else
    /* total cards in other suits <= 7-5: spade flush: */
    if ((i = straightValue[s]) == 0)
        return FLUSH | hi5RanksMask[s];
    else
        return (STRAIGHT_FLUSH - STRAIGHT) + i;
return straightValue[ranks];
}

/**
 * @param 7 cards set 
 * @returns the value of the best 5-card high poker hand from 7 cards.
 */
public static int hand7Eval(CardSet cardSet){
	return hand7Eval(encode(cardSet));
}

/**
* Returns the value of the best 5-card high poker hand from 7 cards.
* @param hand bit mask with one bit set for each of 7 cards.
* @return the value of the best 5-card high poker hand.
*/
public static int hand7Eval(long hand) {
int i, j, ranks;

/* 
 * The low-order 52 bits of hand contains four 13-bit fields, one
 * field per suit.  The high-order 12 bits are clear.   Get the
 * respective fields into variables.
 * We don't care which suit is which; we arbitrarily call them c,d,h,s.
 */
final int c = (int)hand & 0x1FFF;
final int d = ((int)hand >>> 13) & 0x1FFF;
final int h = (int)(hand >>> 26) & 0x1FFF;
final int s = (int)(hand >>> 39);

switch (nbrOfRanks[ranks = c | d | h | s]) {

case 2:
/*
 * quads with trips kicker
 */
    i = c & d & h & s; /* bit for quads */
    return FOUR_OF_A_KIND | hiBotRank[i] | (i ^ ranks);

case 3:
/*
 * trips and pair (full house) with non-playing pair,
 * or two trips (full house) with non-playing singleton,
 * or quads with pair and singleton
 */
    /* bits for singleton, if any, and trips, if any: */
    if (nbrOfRanks[i = c ^ d ^ h ^ s] == 3) {
        /* two trips (full house) with non-playing singleton */
        if (nbrOfRanks[i = c & d] != 2)
            if (nbrOfRanks[i = c & h] != 2)
                if (nbrOfRanks[i = c & s] != 2)
                    if (nbrOfRanks[i = d & h] != 2)
                        if (nbrOfRanks[i = d & s] != 2)
                            i = h & s; /* bits for the trips */
        return FULL_HOUSE | hiBotRank[i] | (i ^ hiRankMask[i]);
    }
    if ((j = c & d & h & s) != 0) /* bit for quads */
        /* quads with pair and singleton */
        return FOUR_OF_A_KIND | hiBotRank[j] | (hiRankMask[ranks ^ j]);
    /* trips and pair (full house) with non-playing pair */
    return FULL_HOUSE | hiBotRank[i] | (hiRankMask[ranks ^ i]);

case 4:
/*
 * three pair and singleton,
 * or trips and pair (full house) and two non-playing singletons,
 * or quads with singleton kicker and two non-playing singletons
 */
    i = c ^ d ^ h ^ s; // the bit(s) of the trips, if any, and singleton(s)
    if (nbrOfRanks[i] == 1) {
        /* three pair and singleton */
        j = ranks ^ i; /* the three bits for the pairs */
        ranks = hiRankMask[j]; /* bit for the top pair */
        j ^= ranks; /* bits for the two bottom pairs */
        return hiTopRankTWO_PAIR[ranks] | hiBotRank[j] | hiRankMask[(hiRankMask[j] ^ j) | i];
    }
    if ((j = c & d & h & s) == 0) {
        // trips and pair (full house) and two non-playing singletons
        i ^= ranks; /* bit for the pair */
        if ((j = (c & d) & (~i)) == 0)
            j = (h & s) & (~i); /* bit for the trips */
        return FULL_HOUSE | hiBotRank[j] | i;
    }
    // quads with singleton kicker and two non-playing singletons
    return FOUR_OF_A_KIND | hiBotRank[j] | (hiRankMask[i]);

case 5:
/*
 * flush and/or straight,
 * or two pair and three singletons,
 * or trips and four singletons
 */
    if ((i = flushAndOrStraight7(ranks, c, d, h, s)) != 0)
        return i;
    i = c ^ d ^ h ^ s; // the bits of the trips, if any, and singletons
    if (nbrOfRanks[i] != 5) {
        /* two pair and three singletons */
        j = i ^ ranks; /* the two bits for the pairs */
        return hiTopRankTWO_PAIR[j] | hiBotRank[hiRankMask[j] ^ j] | hiRankMask[i];
    }
    /* trips and four singletons */
    if ((j = c & d) == 0)
        j = h & s;
    return THREE_OF_A_KIND | hiBotRank[j] | (hi2RanksMask[i ^ j]);

case 6:
/*
 * flush and/or straight,
 * or one pair and three kickers and two nonplaying singletons
 */
    if ((i = flushAndOrStraight7(ranks, c, d, h, s)) != 0)
        return i;
    i = c ^ d ^ h ^ s; /* the bits of the five singletons */
    return PAIR | hiBotRank[ranks ^ i] | hi3RanksMask[i];

case 7:
/*
 * flush and/or straight or no pair
 */
    if ((i = flushAndOrStraight7(ranks, c, d, h, s)) != 0)
        return i;
    return /* NO_PAIR | */ hi5RanksMask[ranks];

} /* end switch */

return 0; /* never reached, but avoids compiler warning */
}


/**
* Returns the value of the best 5-card Razz poker hand from 7 cards.
* @param hand bit mask with one bit set for each of 7 cards.
* @return the value of the best 5-card Razz poker hand.
*/
public static int handRazzEval(long hand) {

// each of the following extracts a 13-bit field from hand and
// rotates it left to position the ace in the least significant bit
final int c = (((int)hand & 0x1FFF) << 1) + (((int)hand ^ 0x1000) >> 12);
final int d = (((int)hand >> 12) & 0x3FFE) + (((int)hand ^ (0x1000 << 13)) >> 25);
final int h = ((int)(hand >> 25) & 0x3FFE) + (int)((hand ^ (0x1000L << 26)) >> 38);
final int s = ((int)(hand >> 38) & 0x3FFE) + (int)((hand ^ (0x1000L << 39)) >> 51);

final int ranks = c | d | h | s;
int i, j;

switch (nbrOfRanks[ranks]) {

case 2:
    /* AAAABBB -- full house */
    i = c & d & h & s; /* bit for quads */
    j = i ^ ranks; /* bit for trips */
    // it can't matter in comparison of results from a 52-card deck,
    // but we return the correct value per relative ranks
    if (i > j)
        return FULL_HOUSE | hiBotRank[i] | (j);
    return FULL_HOUSE | hiBotRank[j] | (i);

case 3:
    /*
     * AAABBBC -- two pair,
     * AAAABBC -- two pair,
     * AAABBCC -- two pair w/ kicker = highest rank.
     */
    /* bits for singleton, if any, and trips, if any: */
    if (nbrOfRanks[i = c ^ d ^ h ^ s] == 3) {
        /* odd number of each rank: AAABBBC -- two pair */
        if (nbrOfRanks[i = c & d] != 2)
            if (nbrOfRanks[i = c & h] != 2)
                if (nbrOfRanks[i = c & s] != 2)
                    if (nbrOfRanks[i = d & h] != 2)
                        if (nbrOfRanks[i = d & s] != 2)
                            i = h & s; /* bits for the trips */
        return hiTopRankTWO_PAIR[i] | hiBotRank[i ^ hiRankMask[i]] | (ranks ^ i);
    }
    if ((j = c & d & h & s) != 0) { /* bit for quads */
        /* AAAABBC -- two pair */
        j = ranks ^ i; /* bits for pairs */
        return hiTopRankTWO_PAIR[j] | hiBotRank[j ^ hiRankMask[j]] | i;
    }
    /* AAABBCC -- two pair w/ kicker = highest rank */
    i = hiRankMask[ranks]; /* kicker bit */
    j = ranks ^ i; /* pairs bits */
    return hiTopRankTWO_PAIR[j] | hiBotRank[j ^ hiRankMask[j]] | i;

case 4:
    /*
     * AABBCCD -- one pair (lowest of A, B, C),
     * AAABBCD -- one pair (A or B),
     * AAAABCD -- one pair (A)
     */
    i = c ^ d ^ h ^ s; /* the bit(s) of the trips, if any,
     and singleton(s) */
    if (nbrOfRanks[i] == 1) {
        /* AABBCCD -- one pair (C with ABD) */
        /* D's bit is in i */
        j = hi2RanksMask[ranks ^ i] | i; /* kickers */
        return PAIR | hiBotRank[ranks ^ j] | j;
    }
    if ((j = c & d & h & s) == 0) {
        /* AAABBCD -- one pair (A or B) */
        i ^= ranks; /* bit for B */
        if ((j = (c & d) & (~i)) == 0)
            j = (h & s) & (~i); /* bit for A */
        if (i < j)
            return PAIR | hiBotRank[i] | (ranks ^ i);
        return PAIR | hiBotRank[j] | (ranks ^ j);
    }
    /* AAAABCD -- one pair (A) */
    return PAIR | hiBotRank[j] | i;

case 5:
    return /* NO_PAIR | */ranks;

case 6:
    return /* NO_PAIR | */lo5RanksMask[ranks];

case 7:
    return /* NO_PAIR | */lo5RanksMask[ranks];

} /* end switch */

return 0; /* never reached, but avoids compiler warning */
}

private static int flushAndOrStraight6(final int ranks, final int c, final int d, final int h, final int s) {

int i, j;

if ((j = nbrOfRanks[c]) > 6 - 5) {
    // there's either a club flush or no flush
    if (j >= 5)
        if ((i = straightValue[c]) == 0)
            return FLUSH | hi5RanksMask[c];
        else
            return (STRAIGHT_FLUSH - STRAIGHT) + i;
} else if ((j += (i = nbrOfRanks[d])) > 6 - 5) {
    if (i >= 5)
        if ((i = straightValue[d]) == 0)
            return FLUSH | hi5RanksMask[d];
        else
            return (STRAIGHT_FLUSH - STRAIGHT) + i;
} else if ((j += (i = nbrOfRanks[h])) > 6 - 5) {
    if (i >= 5)
        if ((i = straightValue[h]) == 0)
            return FLUSH | hi5RanksMask[h];
        else
            return (STRAIGHT_FLUSH - STRAIGHT) + i;
} else
    /* total cards in other suits <= N-5: spade flush: */
    if ((i = straightValue[s]) == 0)
        return FLUSH | hi5RanksMask[s];
    else
        return (STRAIGHT_FLUSH - STRAIGHT) + i;
return straightValue[ranks];
}

/**
* Returns the value of the best 5-card high poker hand from 6 cards.
* @param hand bit mask with one bit set for each of 6 cards.
* @return the value of the best 5-card high poker hand.
*/
public static int hand6Eval(long hand) {

final int c = (int)hand & 0x1FFF;
final int d = ((int)hand >>> 13) & 0x1FFF;
final int h = (int)(hand >>> 26) & 0x1FFF;
final int s = (int)(hand >>> 39);

final int ranks = c | d | h | s;
int i, j, k;

switch (nbrOfRanks[ranks]) {

    case 2: /* quads with pair kicker,
               or two trips (full house) */
            /* bits for trips, if any: */
            if ((nbrOfRanks[i = c ^ d ^ h ^ s]) != 0)
                /* two trips (full house) */
                return FULL_HOUSE | hiBotRank[i]
                        | (i ^ hiRankMask[i]);
            /* quads with pair kicker */
            i = c & d & h & s;  /* bit for quads */
            return FOUR_OF_A_KIND | hiBotRank[i]
                        | (i ^ ranks);

    case 3: /* quads with singleton kicker and non-playing singleton,
               or full house with non-playing singleton,
               or two pair with non-playing pair */
            if ((c ^ d ^ h ^ s) == 0) {
                /* no trips or singletons:  three pair */
                i = hiRankMask[ranks];  /* bit for the top pair */
                k = ranks ^ i;              /* bits for the bottom two pairs */
                j = hiRankMask[k];  /* bit for the middle pair */
                return hiTopRankTWO_PAIR[i]
                    | hiBotRank[j] | (k ^ j); }
            if ((i = c & d & h & s) == 0) {
                /* full house with singleton */
                if ((i = c & d & h) == 0)
                    if ((i = c & d & s) == 0)
                        if ((i = c & h & s) == 0)
                            i = d & h & s; /* bit of trips */
                j = c ^ d ^ h ^ s; /* the bits of the trips
                                      and singleton */
                return FULL_HOUSE | hiBotRank[i]
                        | (j ^ ranks); }
            /* quads with kicker and singleton */
            return FOUR_OF_A_KIND | hiBotRank[i]
                    | (hiRankMask[i ^ ranks]);

    case 4: /* trips and three singletons,
               or two pair and two singletons */
            if ((i = c ^ d ^ h ^ s) != ranks) {
                /* two pair and two singletons */
                j = i ^ ranks;  /* the two bits for the pairs */
                return hiTopRankTWO_PAIR[j]
                        | hiBotRank[hiRankMask[j] ^ j] | hiRankMask[i]; }
            /* trips and three singletons */
            if ((i = c & d) == 0)
                i = h & s; /* bit of trips */
            return THREE_OF_A_KIND | hiBotRank[i]
                    | (hi2RanksMask[i ^ ranks]);

    case 5: /* flush and/or straight,
               or one pair and three kickers and
                one non-playing singleton */
        if ((i = flushAndOrStraight6(ranks, c, d, h, s)) != 0)
            return i;
            i = c ^ d ^ h ^ s; /* the bits of the four singletons */
            return PAIR | hiBotRank[ranks ^ i] | hi3RanksMask[i];

    case 6: /* flush and/or straight or no pair */
        if ((i = flushAndOrStraight6(ranks, c, d, h, s)) != 0)
            return i;
            return /* NO_PAIR | */ hi5RanksMask[ranks];

    } /* end switch */

return 0; /* never reached, but avoids compiler warning */
}

/**
* Returns the value of a 5-card poker hand.
* @param hand bit mask with one bit set for each of 5 cards.
* @return the value of the hand.
*/
public static int hand5Eval(long hand) {

final int c = (int)hand & 0x1FFF;
final int d = ((int)hand >>> 13) & 0x1FFF;
final int h = (int)(hand >>> 26) & 0x1FFF;
final int s = (int)(hand >>> 39);

final int ranks = c | d | h | s;
int i, j;

switch (nbrOfRanks[ranks]) {

    case 2: /* quads or full house */
            i = c & d;              /* any two suits */
            if ((i & h & s) == 0) { /* no bit common to all suits */
                i = c ^ d ^ h ^ s;  /* trips bit */
                return FULL_HOUSE | hiBotRank[i] | (i ^ ranks); }
            else
                /* the quads bit must be present in each suit mask,
                   but the kicker bit in no more than one; so we need
                   only AND any two suit masks to get the quad bit: */
                return FOUR_OF_A_KIND | hiBotRank[i] | (i ^ ranks);

    case 3: /* trips and two kickers,
               or two pair and kicker */
            if ((i = c ^ d ^ h ^ s) == ranks) {
                /* trips and two kickers */
                if ((i = c & d) != 0)
                    return THREE_OF_A_KIND | hiBotRank[i] | (i ^ ranks);
                if ((i = c & h) != 0)
                    return THREE_OF_A_KIND | hiBotRank[i] | (i ^ ranks);
                i = d & h;
                return THREE_OF_A_KIND | hiBotRank[i]
                    | (i ^ ranks); }
            /* two pair and kicker; i has kicker bit */
            j = i ^ ranks;      /* j has pairs bits */
            return hiTopRankTWO_PAIR[j] | hiBotRank[j ^ hiRankMask[j]] | i;

    case 4: /* pair and three kickers */
            i = c ^ d ^ h ^ s; /* kicker bits */
            return PAIR | hiBotRank[ranks ^ i] | i;

    case 5: /* flush and/or straight, or no pair */
            if ((i = straightValue[ranks]) == 0)
                i = ranks;
            if (c != 0) {           /* if any clubs... */
                if (c != ranks)     /*   if no club flush... */
                    return i; }     /*      return straight or no pair value */
            else
                if (d != 0) {
                    if (d != ranks)
                        return i; }
                else
                    if (h != 0) {
                        if (h != ranks)
                            return i; }
                /*  else s == ranks: spade flush */
            /* There is a flush */
            if (i == ranks)
                /* no straight */
                return FLUSH | ranks;
            else
                return (STRAIGHT_FLUSH - STRAIGHT) + i;
}

return 0; /* never reached, but avoids compiler warning */
}

/**
* Returns the Ace-to-5 value of a 5-card low poker hand.
* @param hand bit mask with one bit set for each of 5 cards.
* @return the Ace-to-5 low value of the hand.
*/
public static int hand5Ato5LoEval(long hand) {

// each of the following extracts a 13-bit field from hand and
// rotates it left to position the ace in the least significant bit
final int c = (((int)hand & 0x1FFF) << 1) + (((int)hand ^ 0x1000) >> 12);
final int d = (((int)hand >> 12) & 0x3FFE) + (((int)hand ^ (0x1000 << 13)) >> 25);
final int h = ((int)(hand >> 25) & 0x3FFE) + (int)((hand ^ (0x1000L << 26)) >> 38);
final int s = ((int)(hand >> 38) & 0x3FFE) + (int)((hand ^ (0x1000L << 39)) >> 51);

final int ranks = c | d | h | s;
int i, j;

switch (nbrOfRanks[ranks]) {

    case 2: /* quads or full house */
            i = c & d;              /* any two suits */
            if ((i & h & s) == 0) { /* no bit common to all suits */
                i = c ^ d ^ h ^ s;  /* trips bit */
                return FULL_HOUSE | hiBotRank[i] | (i ^ ranks); }
            else
                /* the quads bit must be present in each suit mask,
                   but the kicker bit in no more than one; so we need
                   only AND any two suit masks to get the quad bit: */
                return FOUR_OF_A_KIND | hiBotRank[i] | (i ^ ranks);

    case 3: /* trips and two kickers,
               or two pair and kicker */
            if ((i = c ^ d ^ h ^ s) == ranks) {
                /* trips and two kickers */
                if ((i = c & d) != 0)
                    return THREE_OF_A_KIND | hiBotRank[i] | (i ^ ranks);
                if ((i = c & h) != 0)
                    return THREE_OF_A_KIND | hiBotRank[i] | (i ^ ranks);
                i = d & h;
                return THREE_OF_A_KIND | hiBotRank[i]
                    | (i ^ ranks); }
            /* two pair and kicker; i has kicker bit */
            j = i ^ ranks;      /* j has pairs bits */
            return hiTopRankTWO_PAIR[j] | hiBotRank[j ^ hiRankMask[j]] | i;

    case 4: /* pair and three kickers */
            i = c ^ d ^ h ^ s; /* kicker bits */
            return PAIR | hiBotRank[ranks ^ i] | i;

    case 5: /* no pair */
            return ranks;
}

return 0; /* never reached, but avoids compiler warning */
}

/**
* Returns the bitwise OR of the suit masks comprising <code>hand</code>; Ace is high.
* @param hand bit mask with one bit set for each of 0 to 52 cards.
* @return the bitwise OR of the suit masks comprising <code>hand</code>.
*/
public static int ranksMask(long hand) {

return (    ((int)hand & 0x1FFF)
        |   (((int)hand >>> 13) & 0x1FFF)
        |   ((int)(hand >>> 26) & 0x1FFF)
        |   (int)(hand >>> 39)
       );       
}

/**
* Returns the bitwise OR of the suit masks comprising <code>hand</code>; Ace is low.
* @param hand bit mask with one bit set for each of 0 to 52 cards.
* @return the bitwise OR of the suit masks comprising <code>hand</code>.
*/
public static int ranksMaskLo(long hand) {

return (    ((((int)hand & 0x1FFF) << 1) + (((int)hand ^ 0x1000) >> 12))
        |   ((((int)hand >> 12) & 0x3FFE) + (((int)hand ^ (0x1000 << 13)) >> 25))
        |   (((int)(hand >> 25) & 0x3FFE) + (int)((hand ^ (0x1000L << 26)) >> 38))
        |   (((int)(hand >> 38) & 0x3FFE) + (int)((hand ^ (0x1000L << 39)) >> 51))
       );       
}

/**
* Returns the 8-or-better low value of a 5-card poker hand or {@link #NO_8_LOW}.
* @param hand bit mask with one bit set for each of up to 7 cards.
* @return the 8-or-better low value of <code>hand</code> or {@link #NO_8_LOW}.
*/
public static int hand8LowEval(long hand) {

return loEvalOrNo8Low[
        /* rotate each 13-bit suit field left to put Ace in LS bit */
        ((((int)hand & 0x1FFF) << 1) + (((int)hand ^ 0x1000) >> 12))
        | ((((int)hand >> 12) & 0x3FFE) + (((int)hand ^ (0x1000 << 13)) >> 25))
        | (((int)(hand >> 25) & 0x3FFE) + (int)((hand ^ (0x1000L << 26)) >> 38))
        | (((int)(hand >> 38) & 0x3FFE) + (int)((hand ^ (0x1000L << 39)) >> 51))
       ];
}

/**
* Returns the 8-or-better low value of the best hand from hole cards and 3 board cards or {@link #NO_8_LOW}.
* @param holeRanks bit mask of the rank(s) of hole cards (Ace is LS bit).
* @param boardRanks bit mask of the rank(s) of board cards (Ace is LS bit).
* @return the 8-or-better low value of the best hand from hole cards and 3 board cards or {@link #NO_8_LOW}.
* @see #ranksMaskLo
*/
public static int Omaha8LowEval(int holeRanks, int boardRanks) {
return loEvalOrNo8Low[lo3RanksMask[boardRanks & ~holeRanks] | holeRanks];
}


//The following exports of accessors to arrays used by the
//evaluation routines may be uncommented if needed.

///**
//* Returns the parameter with all bits except its highest-order bit cleared.
//* @param mask an int in the range 0..0x1FC0 (8128).
//* @return the parameter with all bits except its highest-order bit cleared.
//* @throws IndexOutOfBoundsException if mask < 0 || mask > 0x1FC0.
//*/
//public static int hiCardMask(int mask)
//{
//return hiRankMask[mask];
//}
//
///**
//* Returns the number of bits set in mask.
//* @param mask an int in the range 0..0x1FC0 (8128).
//* @return the number of bits set in mask.
//* @throws IndexOutOfBoundsException if mask < 0 || mask > 0x1FC0.
//*/
//public static int numberOfRanks(int mask)
//{
//return nbrOfRanks[mask];
//}
//
///**
//* Returns the rank (2..14) corresponding to the high-order bit set in mask.
//* @param mask an int in the range 0..0x1FC0 (8128).
//* @return the rank (2..14) corresponding to the high-order bit set in mask.
//* @throws IndexOutOfBoundsException if mask < 0 || mask > 0x1FC0.
//*/
//public static int rankOfHiCard(int mask)
//{
//return hiBotRank[mask] >> BOT_SHIFT;
//}

/** ********** Initialization ********************** */

private static final int ACE_RANK   = 14;

private static final int WHEEL      = 0x0000100F; // A5432

// initializer block
static {
int mask, bitCount;
int shiftReg, i;
int value;

for (mask = 1; mask < ARRAY_SIZE; ++mask) {
    bitCount = 0;
    shiftReg = mask;
    for (i = ACE_RANK - 1; i > 0; --i, shiftReg <<= 1)
        if ((shiftReg & 0x1000) != 0)
            switch (++bitCount) {
            case 1:
                hiTopRankTWO_PAIR[mask] = TWO_PAIR | ((i + 1) << TOP_SHIFT);
                hiBotRank[mask] = (i + 1) << BOT_SHIFT;
                hiRankMask[mask] = 0x1000 >> (ACE_RANK - 1 - i);
                break;
            case 2:
                hi2RanksMask[mask] = (shiftReg & 0x03FFF000) >> (ACE_RANK - 1 - i);
                break;
            case 3:
                hi3RanksMask[mask] = (shiftReg & 0x03FFF000) >> (ACE_RANK - 1 - i);
                break;
            case 5:
                hi5RanksMask[mask] = (shiftReg & 0x03FFF000) >> (ACE_RANK - 1 - i);
            }
    nbrOfRanks[mask] = bitCount;

    bitCount = 0;
    /* rotate the 13 bits left to get ace into LS bit */
    /* we don't need to mask the low 13 bits of the result */
    /* as we're going to look only at the low order 8 bits */
    shiftReg = (mask << 1) + ((mask ^ 0x1000) >> 12);
    value = 0;
    for (i = 0; i < 8; ++i, shiftReg >>= 1)
        if ((shiftReg & 1) != 0) {
            value |= (1 << i); /* undo previous shifts, copy bit */
            if (++bitCount == 5) {
                lo5RanksMask[mask] = value;
                break;
            }
            if (bitCount == 3)
                lo3RanksMask[mask] = value;
        }
    loEvalOrNo8Low[mask] = (bitCount == 5) ? value : NO_8_LOW;
}
for (mask = 0x1F00/* A..T */; mask >= 0x001F/* 6..2 */; mask >>= 1)
    setStraight(mask);
setStraight(WHEEL); /* A,5..2 */
}

private static void setStraight(int ts) {
/* must call with ts from A..T to 5..A in that order */

    int es, i, j;

    for (i = 0x1000; i > 0; i >>= 1)
        for (j = 0x1000; j > 0; j >>= 1) {
            es = ts | i | j; /* 5 straight bits plus up to two other bits */
            if (straightValue[es] == 0)
                if (ts == WHEEL)
                    straightValue[es] = STRAIGHT | (5 << BOT_SHIFT);
                else
                    straightValue[es] = STRAIGHT | hiBotRank[ts];
        }
}
}
