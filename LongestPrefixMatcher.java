import java.io.*;

public class LongestPrefixMatcher {
    public static final String PIETER_ROUTES = "C:\\Users\\Pieter\\Documents\\UTwente\\Jaar 1\\Module 3 - Netwerksystemen\\Week 4\\LPM\\routes.txt";
    public static final String PIETER_LOOKUP = "C:\\Users\\Pieter\\Documents\\UTwente\\Jaar 1\\Module 3 - Netwerksystemen\\Week 4\\LPM\\lookup.bin";
    public static final String TOKEN = "PieterBosSophieLathouwers_k1kya";

    public static char[] lastResult;

    public static void main(String[] args) throws IOException {
        Router router = new Router(new File(PIETER_ROUTES));
        Trie trie = router.getTrie();

        InputStream input = new FileInputStream(PIETER_LOOKUP);
        StringBuilder builder = new StringBuilder();

        System.out.println(TOKEN);

        int i = 0;

        while(input.available() >= 4) {
            lastResult = Trie.NO_DATA;
            trie.lookUp(input.read() << 24 | input.read() << 16 | input.read() << 8 | input.read());
            builder.append(lastResult);

            i++;

            if(i % 10000 == 0) {
                System.out.print(builder.toString());
                builder = new StringBuilder();
            }
        }

        System.out.print(builder.toString());

        throw null;
    }

    public static class IPUtility {
        public static int getIp(String[] parts) {
            return Integer.parseInt(parts[0]) << 24
                 | Integer.parseInt(parts[1]) << 16
                 | Integer.parseInt(parts[2]) << 8
                 | Integer.parseInt(parts[3]);
        }

        public static int getIp(int[] parts) {
            return parts[0] << 24
                 | parts[1] << 16
                 | parts[2] << 8
                 | parts[3];
        }
    }

    public static class Trie {
        public static final char[] NO_DATA = new char[] { '-', '1', '\n' };

        private char[] data;

        private Trie zero;
        private Trie one;

        public Trie() {
            this.data = NO_DATA;
        }

        public void setData(int data) {
            this.data = (Integer.toString(data) + "\n").toCharArray();
        }

        public void insert(int location, int length, int data) {
            if(length == 0) {
                setData(data);
            } else {
                int bit = location & (1 << 31);

                if(bit == 0) {
                    if(zero == null) {
                        zero = new Trie();
                    }

                    zero.insert(location << 1, length - 1, data);
                } else {
                    if(one == null) {
                        one = new Trie();
                    }

                    one.insert(location << 1, length - 1, data);
                }
            }
        }

        public void lookUp(int location) {
            Trie current = this;

            while(current != null) {
                int bit = location & (1 << 31);
                location <<= 1;

                if(current.data != NO_DATA) {
                    LongestPrefixMatcher.lastResult = current.data;
                }

                if(bit == 0) {
                    current = current.zero;
                } else {
                    current = current.one;
                }
            }
        }

        @Override
        public String toString() {
            return "Trie(0=" + (zero == null ? "null" : zero.toString()) + ", 1=" + (one == null ? "null" : one.toString()) + ")";
        }
    }

    public static class Router {
        private Trie routes = new Trie();

        public Router(File routeFile) throws IOException {
            BufferedReader reader = new BufferedReader(new FileReader(routeFile));

            String line;

            while((line = reader.readLine()) != null) {
                String[] lineParts = line.split("[\t/]");

                String ipString = lineParts[0];
                int length = Integer.parseInt(lineParts[1], 10);
                int outgoingInterface = Integer.parseInt(lineParts[2], 10);

                String[] parts = ipString.split("\\.");

                int ip = Integer.parseInt(parts[0]) << 24
                       | Integer.parseInt(parts[1]) << 16
                       | Integer.parseInt(parts[2]) << 8
                       | Integer.parseInt(parts[3]);

                routes.insert(ip, length, outgoingInterface);
            }
        }

        public Trie getTrie() {
            return routes;
        }
    }
}