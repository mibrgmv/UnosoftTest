import java.io.*;
import java.util.*;

public class Main {
    private static int maxLineLength = 0;
    private static List<List<String>> lines;

    public static void main(String[] args) throws IOException {
        long startingTime = System.currentTimeMillis();
        lines = readLinesFromFile("lng.txt");
        int n = lines.size();
        int currentComponent = 1;

        /* каждой строке соответствует номер группы */
        Map<Integer, Integer> linesComponents = new HashMap<>();

        /* каждой группе соответствует сет индексов строк, входящих в эту группу */
        Map<Integer, Set<Integer>> groups = new HashMap<>();
        for (int i = 0; i < maxLineLength; i++) {

            /* элементу соответствует сет индексов строк, в которых он встречался на i-й позиции */
            Map<String, Set<Integer>> elementGroup = new HashMap<>();
            for (int j = 0; j < n; j++) {
                if (i < lines.get(j).size() && !lines.get(j).get(i).isEmpty() && !lines.get(j).get(i).equals("\"\"")) {
                    if (!elementGroup.containsKey(lines.get(j).get(i))) {
                        elementGroup.put(lines.get(j).get(i), new HashSet<>());
                    }
                    elementGroup.get(lines.get(j).get(i)).add(j);
                }
            }
            for (Set<Integer> groupsMembers : elementGroup.values()) {
                Set<Integer> indices = new HashSet<>();
                for (Integer index : groupsMembers) {
                    if (!linesComponents.containsKey(index)) {
                        linesComponents.put(index, currentComponent);
                        groups.putIfAbsent(currentComponent, groupsMembers);
                    }
                    indices.add(linesComponents.get(index));
                }
                ++currentComponent;
                List<Integer> indicesList = indices.stream().toList();
                if (indicesList.isEmpty()) {
                    continue;
                }

                /* для случаев, когда несколько групп связаны через общий элемент */
                int mainIndex = indicesList.getFirst();
                Set<Integer> subgroupMembers = new HashSet<>();
                for (int j = 1; j < indicesList.size(); j++) {
                    int groupId = indicesList.get(j);
                    for (Integer index : groups.get(groupId)) {
                        linesComponents.put(index, mainIndex);
                    }
                    subgroupMembers.addAll(groups.get(groupId));
                    groups.remove(groupId);
                }
                groups.get(mainIndex).addAll(groupsMembers);
                groups.get(mainIndex).addAll(subgroupMembers);
            }
        }

        List<Set<Integer>> groupList = new ArrayList<>(groups.values());
        int numberOfGroups = (int) groupList.stream().filter(a -> a.size() > 1).count();
        writeGroupsToFile("groups.txt", numberOfGroups, groupList);
        double executionTimeSeconds = (System.currentTimeMillis() - startingTime) / 1000.0;
        System.out.println("number of components: " + numberOfGroups);
        System.out.println("runtime: " + executionTimeSeconds + " s");
    }

    private static List<List<String>> readLinesFromFile(String filename) {
        Set<List<String>> lines = new HashSet<>();
        try {
            BufferedReader br = new BufferedReader(new FileReader(filename));
            String line;
            while ((line = br.readLine()) != null) {
                if (line.matches("^[(\"\\d*.?\\d*\";)\\w]*[(\"\\d*.?\\d*\";)\\w]$")) {
                    List<String> linesList = Arrays.stream(line.split(";")).toList();
                    lines.add(linesList);
                    maxLineLength = Math.max(maxLineLength, linesList.size());
                }
            }
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
        return lines.stream().toList();
    }

    private static void writeGroupsToFile(String filename, int numberOfGroups, List<Set<Integer>> groups) {
        groups.sort((a, b) -> b.size() - a.size());
        int groupCount = 0;
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(filename))) {
            bw.write("" + numberOfGroups + '\n');
            for (Set<Integer> group : groups) {
                if (group.size() <= 1) {
                    continue;
                }
                bw.write("Group " + ++groupCount + '\n');
                for (int lineIndex : group) {
                    int lineSize = lines.get(lineIndex).size();
                    for (int elementIndex = 0; elementIndex < lineSize; elementIndex++) {
                        bw.write(lines.get(lineIndex).get(elementIndex));
                        if (elementIndex + 1 < lineSize) bw.write(";");
                        else bw.newLine();
                    }
                }
                bw.newLine();
            }
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }
}
