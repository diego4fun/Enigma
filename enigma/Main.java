package enigma;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;

import java.util.Scanner;
import java.util.List;
import java.util.ArrayList;
import java.util.NoSuchElementException;

import ucb.util.CommandArgs;

import static enigma.EnigmaException.*;

/** Enigma simulator.
 *  @author Oumar Balde
 */
public final class Main {

    /** Process a sequence of encryptions and decryptions, as
     *  specified by ARGS, where 1 <= ARGS.length <= 3.
     *  ARGS[0] is the name of a configuration file.
     *  ARGS[1] is optional; when present, it names an input file
     *  containing messages.  Otherwise, input comes from the standard
     *  input.  ARGS[2] is optional; when present, it names an output
     *  file for processed messages.  Otherwise, output goes to the
     *  standard output. Exits normally if there are no errors in the input;
     *  otherwise with code 1. */
    public static void main(String... args) {
        try {
            CommandArgs options =
                new CommandArgs("--verbose --=(.*){1,3}", args);
            if (!options.ok()) {
                throw error("Usage: java enigma.Main [--verbose] "
                            + "[INPUT [OUTPUT]]");
            }

            _verbose = options.contains("--verbose");
            new Main(options.get("--")).process();
            return;
        } catch (EnigmaException excp) {
            System.err.printf("Error: %s%n", excp.getMessage());
        }
        System.exit(1);
    }

    /** Open the necessary files for non-option arguments ARGS (see comment
      *  on main). */
    Main(List<String> args) {
        _config = getInput(args.get(0));

        if (args.size() > 1) {
            _input = getInput(args.get(1));
        } else {
            _input = new Scanner(System.in);
        }

        if (args.size() > 2) {
            _output = getOutput(args.get(2));
        } else {
            _output = System.out;
        }
    }

    /** Return a Scanner reading from the file named NAME. */
    private Scanner getInput(String name) {
        try {
            return new Scanner(new File(name));
        } catch (IOException excp) {
            throw error("could not open %s", name);
        }
    }

    /** Return a PrintStream writing to the file named NAME. */
    private PrintStream getOutput(String name) {
        try {
            return new PrintStream(new File(name));
        } catch (IOException excp) {
            throw error("could not open %s", name);
        }
    }

    /** Configure an Enigma machine from the contents of configuration
     *  file _config and apply it to the messages in _input, sending the
     *  results to _output. */
    private void process() {

        Machine machine = readConfig();
        String setupLine = _input.nextLine();
        String msg = "";
        setUp(machine, setupLine);

        while (_input.hasNextLine()) {
            String nextLine = _input.nextLine();
            if (nextLine.contains("*")) {
                setUp(machine, nextLine);
            } else {
                msg = machine.convert(
                        nextLine.replaceAll(" ", ""));
                printMessageLine(msg);
            }
        }
    }

    /** Return an Enigma machine configured from the contents of configuration
     *  file _config. */
    private Machine readConfig() {
        try {
            String chars = _config.nextLine();
            if (chars.contains("*") || chars.contains("(")
                    || chars.contains(")")) {
                throw new EnigmaException("Wrong alphabet formatting");
            }
            _alphabet = new Alphabet(chars);

            Scanner lineScanner = new Scanner(_config.nextLine());
            int numRotors = lineScanner.nextInt();
            int pawls = lineScanner.nextInt();

            ArrayList<Rotor> allRotors = new ArrayList<Rotor>();
            while (_config.hasNext()) {
                allRotors.add(readRotor());
            }

            return new Machine(_alphabet, numRotors, pawls, allRotors);

        } catch (NoSuchElementException excp) {
            throw error("configuration file truncated");
        }
    }

    /** Return a rotor, reading its description from _config. */
    private Rotor readRotor() {
        try {

            String name = _config.next();
            String rotorType = _config.next();
            String cycles = "";

            while (_config.hasNext("\\(.*\\)")) {
                cycles += _config.next();
            }

            Permutation perm = new Permutation(cycles, _alphabet);

            if (rotorType.charAt(0) == 'M') {
                String notches = "";
                for (int i = 1; i < rotorType.length(); i++) {
                    notches += rotorType.charAt(i);
                }
                return new MovingRotor(name, perm, notches);
            } else if (rotorType.charAt(0) == 'N') {
                return new FixedRotor(name, perm);
            } else if (rotorType.charAt(0) == 'R') {
                return new Reflector(name, perm);
            } else {
                throw new EnigmaException("Bad rotor description");
            }

        } catch (NoSuchElementException excp) {
            throw error("bad rotor description");
        }
    }

    /** Set M according to the specification given on SETTINGS,
     *  which must have the format specified in the assignment. */
    private void setUp(Machine M, String settings) {

        try {
            String[] settingArr = settings.split(" ");

            if (settingArr.length - 1 < M.numRotors()) {
                throw new EnigmaException("Wrong input format");
            }
            if (!settingArr[0].equals("*")) {
                throw new EnigmaException("Input should begin with '*'");
            }

            String cycles = "";
            String[] rotorNames = new String[M.numRotors()];
            for (int i = 1; i <= M.numRotors(); i++) {
                rotorNames[i - 1] = settingArr[i];
            }


            M.insertRotors(rotorNames);

            int movingRotors = 0; int fixedRotors = 0;
            for (int i = 0; i < M.numRotors(); i++) {
                if (M.getRotor(i).rotates()) {
                    movingRotors++;
                } else {
                    fixedRotors++;
                }
            }

            if (movingRotors != M.numPawls()
                    || fixedRotors != M.numRotors() - M.numPawls()) {
                throw new EnigmaException("Wrong number of "
                        + "moving/non-moving rotors");
            }

            for (int i = M.numRotors() + 2; i < settingArr.length; i++) {
                cycles += settingArr[i];
            }
            Permutation plugboard = new Permutation(cycles, _alphabet);

            M.setRotors(settingArr[M.numRotors() + 1]);
            M.setPlugboard(plugboard);
            M.insertRotors(rotorNames);
        } catch (ArrayIndexOutOfBoundsException excp) {
            throw error("Wrong input format");
        } catch (EnigmaException excp) {
            throw error("Wrong input format");
        }

    }

    /** Return true iff verbose option specified. */
    static boolean verbose() {
        return _verbose;
    }

    /** Print MSG in groups of five (except that the last group may
     *  have fewer letters). */
    private void printMessageLine(String msg) {
        msg = msg.replace(" ", "");
        String result = "";
        while (msg.length() > 5) {
            result += msg.substring(0, 5) + " ";
            msg = msg.substring(5);
        }
        result += msg;
        _output.println(result);
    }

    /** Alphabet used in this machine. */
    private Alphabet _alphabet;

    /** Source of input messages. */
    private Scanner _input;

    /** Source of machine configuration. */
    private Scanner _config;

    /** File for encoded/decoded messages. */
    private PrintStream _output;

    /** True if --verbose specified. */
    private static boolean _verbose;
}
