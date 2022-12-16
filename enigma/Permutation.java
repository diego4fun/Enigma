package enigma;

import static enigma.EnigmaException.*;

/** Represents a permutation of a range of integers starting at 0 corresponding
 *  to the characters of an alphabet.
 *  @author Oumar Balde
 */
class Permutation {

    /** Set this Permutation to that specified by CYCLES, a string in the
     *  form "(cccc) (cc) ..." where the c's are characters in ALPHABET, which
     *  is interpreted as a permutation in cycle notation.  Characters in the
     *  alphabet that are not included in any cycle map to themselves.
     *  Whitespace is ignored. */
    Permutation(String cycles, Alphabet alphabet) {
        _alphabet = alphabet;
        _cycles = cycles;
    }

    /** Add the cycle c0->c1->...->cm->c0 to the permutation, where CYCLE is
     *  c0c1...cm. */
    private void addCycle(String cycle) {
        _cycles += " " + "(" + cycle + ")";
    }

    /** Return the value of P modulo the size of this permutation. */
    final int wrap(int p) {
        int r = p % size();
        if (r < 0) {
            r += size();
        }
        return r;
    }

    /** Returns the size of the alphabet I permute. */
    int size() {
        return _alphabet.size();
    }

    /** Return the result of applying this permutation to P modulo the
     *  alphabet size. */
    int permute(int p) {
        char letter = _alphabet.toChar(wrap(p));
        int index = _cycles.indexOf(letter);
        if (index == -1) {
            return wrap(p);
        } else if (_cycles.charAt(index + 1) != ')') {
            return _alphabet.toInt(_cycles.charAt(index + 1));
        } else {
            while (_cycles.charAt(index) != '(') {
                index--;
            }
            return _alphabet.toInt(_cycles.charAt(index + 1));
        }
    }

    /** Return the result of applying the inverse of this permutation
     *  to  C modulo the alphabet size. */
    int invert(int c) {
        char letter = _alphabet.toChar(wrap(c));
        int index = _cycles.indexOf(letter);
        if (index == -1) {
            return wrap(c);
        } else if (_cycles.charAt(index - 1) != '(') {
            return _alphabet.toInt(_cycles.charAt(index - 1));
        } else {
            while (_cycles.charAt(index) != ')') {
                index++;
            }
            return _alphabet.toInt(_cycles.charAt(index - 1));
        }
    }

    /** Return the result of applying this permutation to the index of P
     *  in ALPHABET, and converting the result to a character of ALPHABET. */
    char permute(char p) {
        int index = _alphabet.toInt(p);
        int permIndex = permute(index);
        return _alphabet.toChar(permIndex);
    }

    /** Return the result of applying the inverse of this permutation to C. */
    char invert(char c) {
        int index = _alphabet.toInt(c);
        int permIndex = invert(index);
        return _alphabet.toChar(permIndex);
    }

    /** Return the alphabet used to initialize this Permutation. */
    Alphabet alphabet() {
        return _alphabet;
    }

    /** Return true iff this permutation is a derangement (i.e., a
     *  permutation for which no value maps to itself). */
    boolean derangement() {
        for (int i = 0; i < size(); i++) {
            if (permute(_alphabet.toChar(i)) == _alphabet.toChar(i)) {
                return false;
            }
        }
        return true;
    }

    /** Alphabet of this permutation. */
    private Alphabet _alphabet;

    /**
     * Return the cycles used to initialize this Permutation.
     */
    String cycles() {
        return _cycles;
    }

    /**
     * Cycles of this permutation.
     */
    private String _cycles;
}
