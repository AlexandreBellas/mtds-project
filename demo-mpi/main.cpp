#include <mpi.h>
#include <string.h>

#include <iostream>
#include <map>
#include <string>
#include <vector>

using namespace std;

int maxLenWord = 50;

void treatWordReadFromFile(string *s) {
  // Remove non-alphanumeric characters
  for (string::iterator i = (*s).begin(); i != (*s).end(); i++) {
    if (!isalnum((*s).at(i - (*s).begin()))) {
      (*s).erase(i);
      i--;
    }
  }

  // Make it lower case
  for (auto &c : *s) {
    c = tolower(c);
  }
}

string serializeWords(map<string, int> words) {
  string output;
  for (auto const &[key, val] : words) {
    output += key + ":" + to_string(val) + ";";
  }

  return output;
}

char **serializeOrganizedWords(map<string, vector<int>> wordMap, int rows, int columns) {
  char **output;
  output = (char **)malloc(rows * sizeof(char *));

  // Loop through map and allocate in the matrix
  int i = 0;
  for (auto const &[word, countList] : wordMap) {
    output[i] = (char *)malloc(columns * sizeof(char));

    string serializedPair = word + ":";

    for (int count : countList) {
      serializedPair += to_string(count) + ",";
    }

    strcpy(output[i], serializedPair.c_str());

    i++;
  }

  return output;
}

map<string, vector<int>> organizeWordsFrom(char *serializedWordCounts) {
  map<string, vector<int>> wordMap;
  map<string, vector<int>>::iterator wordMapIt;
  string strSerializedWordCounts(serializedWordCounts);

  string delimiter = ";";
  string innerDelimeter = ":";
  size_t pos = 0;
  string token;

  // Split string from ';' delimiter
  while ((pos = strSerializedWordCounts.find(delimiter)) != string::npos) {
    token = strSerializedWordCounts.substr(0, pos);

    // Split token from ':' delimiter
    string word = token.substr(0, token.find(innerDelimeter));

    const char *strCount = token.substr(token.find(innerDelimeter) + 1, token.size()).c_str();
    int count = atoi(strCount);

    // Insert the counts following each collision
    wordMapIt = wordMap.find(word);
    if (wordMapIt != wordMap.end()) {
      wordMapIt->second.push_back(count);
    } else {
      vector<int> newIntList = {count};
      wordMap.insert(pair<string, vector<int>>(word, newIntList));
    }

    strSerializedWordCounts.erase(0, pos + delimiter.length());
  }

  return wordMap;
}

pair<string, vector<int>> deserializeWordCounts(char *serializedWordCounts) {
  vector<int> counts;
  vector<int>::iterator countsIt;
  string strSerializedWordCounts(serializedWordCounts);

  string outterDelimiter = ":";
  string innerDelimeter = ",";
  size_t outterPos = 0;
  size_t innerPos = 0;

  string word;
  string strCounts;

  // Split string from outter delimiter
  outterPos = strSerializedWordCounts.find(outterDelimiter);

  word = strSerializedWordCounts.substr(0, outterPos);
  strCounts = strSerializedWordCounts.substr(outterPos + outterDelimiter.length(), strSerializedWordCounts.size());

  // Split token from inner delimiter
  while ((innerPos = strCounts.find(innerDelimeter)) != string::npos) {
    string strCountNumber = strCounts.substr(0, innerPos);

    // const char *strCount = word.substr(word.find(innerDelimeter) + 1, word.size()).c_str();
    int count = atoi(strCountNumber.c_str());

    counts.push_back(count);

    strCounts.erase(0, innerPos + innerDelimeter.length());
  }

  pair<string, vector<int>> output(word, counts);

  return output;
}

int main(int argc, char *argv[]) {
  // Global configuration
  MPI_Init(NULL, NULL);

  const int root = 0;
  int my_rank = 0, world_size;
  MPI_Comm_rank(MPI_COMM_WORLD, &my_rank);
  MPI_Comm_size(MPI_COMM_WORLD, &world_size);

  char buff[maxLenWord];
  map<string, int> words = {};
  map<string, int>::iterator wordsIt;

  // Prepare to read and open each file
  FILE *file;

  const char *fileNamePrefixRaw = to_string(my_rank + 1).c_str();
  char *fileNamePrefix = strdup(fileNamePrefixRaw);
  char *fileNameDirectory = strdup("./files/");
  char *fileName = strcat(fileNameDirectory, strcat(fileNamePrefix, ".txt"));

  file = fopen(fileName, "r");

  // Read respective file
  while (fscanf(file, "%s", buff) == 1) {
    string readWord(buff);
    treatWordReadFromFile(&readWord);

    wordsIt = words.find(readWord);
    if (wordsIt != words.end()) {
      wordsIt->second++;
    } else {
      words.insert(pair<string, int>(readWord, 1));
    }
  }

  // Prepare to send to root; "map" is serialized to simplify the message
  string serializedWordList = serializeWords(words);
  int serializedWordListSize = serializedWordList.size();

  // Gather count of chars of each serialized word list
  int *numChars = NULL;
  if (my_rank == root) {
    numChars = (int *)malloc(world_size * sizeof(int));
  }

  MPI_Gather(&serializedWordListSize, 1, MPI_INT, numChars, 1, MPI_INT, root, MPI_COMM_WORLD);

  // Figure out the total length of string, and displacements for each rank
  int totlen = 0;
  int *displs = NULL;
  char *totalstring = NULL;

  if (my_rank == root) {
    displs = (int *)malloc(world_size * sizeof(int));

    displs[0] = 0;
    totlen += numChars[0] + 1;  // +1 for the '\0' character in the end

    for (int i = 1; i < world_size; i++) {
      totlen += numChars[i];
      displs[i] = displs[i - 1] + numChars[i - 1];
    }

    totalstring = (char *)malloc(totlen * sizeof(char));
    for (int i = 0; i < totlen - 1; i++) {
      totalstring[i] = ' ';
    }

    totalstring[totlen - 1] = '\0';
  }

  // Gather all strings after preparing the receiver string
  MPI_Gatherv(serializedWordList.c_str(), serializedWordListSize, MPI_CHAR,
              totalstring, numChars, displs, MPI_CHAR,
              root, MPI_COMM_WORLD);

  // Organize words for posterior counting
  map<string, vector<int>> wordMap;
  char **organizedCountedWordList = NULL;
  int totalNumWords;

  int rows;
  int columns = maxLenWord + 2 * world_size;

  if (my_rank == root) {
    // Organize words in a map of counts
    wordMap = organizeWordsFrom(totalstring);

    // Build matrix of words
    rows = wordMap.size();
    organizedCountedWordList = serializeOrganizedWords(wordMap, rows, columns);
  }

  // Share with all threads the amount of words
  MPI_Bcast(&rows, 1, MPI_INT, root, MPI_COMM_WORLD);

  MPI_Barrier(MPI_COMM_WORLD);

  // Send each part of the serialized word list to each process, and sum the counts
  char *serializedWordCounts = NULL;
  serializedWordCounts = (char *)malloc(columns * sizeof(char));

  int process = 0;

  for (int i = 0; i < rows; i++) {
    process++;

    // Circular logic
    if (process == world_size) {
      process = 1;
    }

    // Root process task
    if (my_rank == root) {
      MPI_Send(organizedCountedWordList[i], columns, MPI_CHAR, process, 0, MPI_COMM_WORLD);
    }
    // All other processes task
    else if (my_rank == process) {
      MPI_Status status;
      MPI_Recv(serializedWordCounts, columns, MPI_CHAR, root, 0, MPI_COMM_WORLD, &status);

      pair<string, vector<int>> wordCounts = deserializeWordCounts(serializedWordCounts);

      int sum = 0;
      for (auto count : wordCounts.second) {
        sum += count;
      }

      string serializedResult = wordCounts.first + ":" + to_string(sum);

      // Fill the rest of the string with empty character avoiding memory leak
      while (serializedResult.size() < columns) {
        serializedResult += '\0';
      }

      MPI_Send(serializedResult.c_str(), serializedResult.size(), MPI_CHAR, root, 0, MPI_COMM_WORLD);
    }
  }

  // Last task: sum all counts and write to file
  if (my_rank == root) {
    // Receive message from all processes in root
    process = 0;

    string strResultText;

    for (int i = 0; i < rows; i++) {
      process++;

      if (process == world_size) {
        process = 1;
      }

      MPI_Status status;
      MPI_Recv(serializedWordCounts, columns, MPI_CHAR, process, 0, MPI_COMM_WORLD, &status);

      strResultText += serializedWordCounts + string("; ");
    }

    // Write results to file
    FILE *resultFile;
    resultFile = fopen("./files/result.txt", "w");

    fprintf(resultFile, "%s", strResultText.c_str());

    fclose(resultFile);

    // Free memory from prior "gather" process
    free(totalstring);
    free(displs);
    free(numChars);

    // Free memory from posterior "send" and "receive" process
    for (int i = 0; i < rows; i++) {
      free(organizedCountedWordList[i]);
    }

    free(organizedCountedWordList);
  }

  free(serializedWordCounts);

  // Close all files
  fclose(file);

  // Wait for all the threads to terminate and finish MPI
  MPI_Barrier(MPI_COMM_WORLD);
  MPI_Finalize();

  return 0;
}