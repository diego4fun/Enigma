package enigma;


import java.util.ArrayList;
import java.util.Collection;

import static enigma.EnigmaException.*;

/** Class that represents a complete enigma machine.
 *  @author Oumar Balde
 */
class Machine {

    /** A new Enigma machine with alphabet ALPHA, 1 < NUMROTORS rotor slots,
     *  and 0 <= PAWLS < NUMROTORS pawls.  ALLROTORS contains all the
     *  available rotors. */
    Machine(Alphabet alpha, int numRotors, int pawls,
            Collection<Rotor> allRotors) {
        _alphabet = alpha;
        _numRotors = numRotors;
        if (numRotors() < 1) {
            throw new EnigmaException("Number of rotors is invalid");
        }
        _pawls = pawls;
        if (pawls < 0 || pawls >= numRotors) {
            throw new EnigmaException("Invalid number of pawls");
        }
        _rotorsInSlot = new ArrayList<Rotor>();
        _allRotors = new ArrayList<Rotor>();
        for (Rotor rotor : allRotors) {
            _allRotors.add(rotor);
        }
    }

    /** Return the number of rotor slots I have. */
    int numRotors() {
        return _numRotors;
    }

    /** Return the number pawls (and thus rotating rotors) I have. */
    int numPawls() {
        return _pawls;
    }

    /** Return Rotor #K, where Rotor #0 is the reflector, and Rotor
     *  #(numRotors()-1) is the fast Rotor.  Modifying this Rotor has
     *  undefined results. */
    Rotor getRotor(int k) {
        try {
            return _rotorsInSlot.get(k);
        } catch (IndexOutOfBoundsException excp) {
            throw error("Wrong input format");
        }

    }

    Alphabet alphabet() {
        return _alphabet;
    }

    /** Set my rotor slots to the rotors named ROTORS from my set of
     *  available rotors (ROTORS[0] names the reflector).
     *  Initially, all rotors are set at their 0 setting. */
    void insertRotors(String[] rotors) {
        if (numRotors() != rotors.length) {
            throw new EnigmaException("Wrong number of Rotors "
                    + "passed by setting");
        } else {
            for (int i = 0; i < rotors.length; i++) {
                for (int j = 0; j < _allRotors.size(); j++) {
                    Rotor rotor = _allRotors.get(j);
                    if (rotor.name().equals(rotors[i])) {
                        _rotorsInSlot.add(rotor);
                    }
                }
            }
        }

        if (!getRotor(0).reflecting()) {
            throw new EnigmaException("Rotor 0 is supposed "
                    + "to be the reflector");
        }

        if (!getRotor(numRotors() - 1).rotates()) {
            throw new EnigmaException("The last rotor should "
                    + "be a moving rotor");
        }
    }

    /** Set my rotors according to SETTING, which must be a string of
     *  numRotors()-1 characters in my alphabet. The first letter refers
     *  to the leftmost rotor setting (not counting the reflector).  */
    void setRotors(String setting) {

        if (numRotors() - 1 != setting.length()) {
            throw new EnigmaException("Length is wrong for Rotor setting.");
        } else {
            for (int i = 1; i < numRotors(); i++) {
                char letter = setting.charAt(i - 1);
                if (alphabet().toInt(letter) == -1) {
                    throw new EnigmaException("Letter"
                            + letter + "not in this alphabet");
                }
                getRotor(i).set(letter);
            }
        }
    }

    /** Return the current plugboard's permutation. */
    Permutation plugboard() {
        return _plugBoardPermutation;
    }

    /** Set the plugboard to PLUGBOARD. */
    void setPlugboard(Permutation plugboard) {
        _plugBoardPermutation = plugboard;
    }

    /** Returns the result of converting the input character C (as an
     *  index in the range 0..alphabet size - 1), after first advancing
     *  the machine. */
    int convert(int c) {
        advanceRotors();
        if (Main.verbose()) {
            System.err.printf("[");
            for (int r = 1; r < numRotors(); r += 1) {
                System.err.printf("%c",
                        alphabet().toChar(getRotor(r).setting()));
            }
            System.err.printf("] %c -> ", alphabet().toChar(c));
        }
        c = plugboard().permute(c);
        if (Main.verbose()) {
            System.err.printf("%c -> ", alphabet().toChar(c));
        }
        c = applyRotors(c);
        c = plugboard().permute(c);
        if (Main.verbose()) {
            System.err.printf("%c%n", alphabet().toChar(c));
        }
        return c;
    }

    /** Advance all rotors to their next position. */
    private void advanceRotors() {
        for (int i = 0; i < numRotors(); i++) {
            if (i == numRotors() - 1) {
                getRotor(i).advance();
            } else if (getRotor(i).rotates() && getRotor(i + 1).atNotch()) {
                getRotor(i).advance();
                getRotor(i + 1).advance();
                i++;
            }
        }
    }

    /** Return the result of applying the rotors to the character C (as an
     *  index in the range 0..alphabet size - 1). */
    private int applyRotors(int c) {
        int p = getRotor(numRotors() - 1).convertForward(c);

        for (int i = numRotors() - 2; i >= 0; i--) {
            p = getRotor(i).convertForward(p);
        }

        for (int i = 1; i < numRotors(); i++) {
            p = getRotor(i).convertBackward(p);
        }

        return p;
    }

    /** Returns the encoding/decoding of MSG, updating the state of
     *  the rotors accordingly. */
    String convert(String msg) {
        String cypheredMSG = "";
        for (int i = 0; i < msg.length(); i++) {
            int letter = alphabet().toInt(msg.charAt(i));
            if (letter == -1) {
                cypheredMSG += msg.charAt(i);
            } else {
                cypheredMSG += alphabet().toChar(convert(letter));
            }
        }
        return cypheredMSG;
    }

    /** Common alphabet of my rotors. */
    private final Alphabet _alphabet;

    /** This Machine's number of Rotors. */
    private int _numRotors;

    /** This Machine's number of Pawls. */
    private int _pawls;

    /** An ArrayList containing all the Rotors from this Machine. */
    private ArrayList<Rotor> _allRotors;

    /** The plugboard's Permutation. */
    private Permutation _plugBoardPermutation;

    /** The Machine's rotors that are actually being used. */
    private ArrayList<Rotor> _rotorsInSlot;
}
