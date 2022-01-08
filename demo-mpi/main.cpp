#include <mpi.h>
#include <string.h>

#include <iostream>
#include <map>
#include <string>
#include <vector>

using namespace std;

typedef struct {
  char *content;
  int count = 0;
} word;

string convertToString(char *a) {
  int i;
  string s = "";

  for (i = 0; a[i] != '\0'; i++) {
    s = s + a[i];
  }

  return s;
}

void removeNonAlphanumeric(string *s) {
  for (string::iterator i = (*s).begin(); i != (*s).end(); i++) {
    if (!isalnum((*s).at(i - (*s).begin()))) {
      (*s).erase(i);
      i--;
    }
  }
}

string serializeWords(map<string, int> words) {
  string output;
  for (auto const &[key, val] : words) {
    output += key + ":" + to_string(val) + ";";
  }

  return output;
}

map<string, vector<int>> deserializeWords(char *input) {
  map<string, vector<int>> output;
  map<string, vector<int>>::iterator outputIt;
  string words(input);

  string delimiter = ";";
  size_t pos = 0;
  string token;

  // Split string from ';' delimiter
  while ((pos = words.find(delimiter)) != string::npos) {
    token = words.substr(0, pos);

    // Split token from ':' delimiter
    string innerDelimeter = ":";

    string word = token.substr(0, token.find(innerDelimeter));

    const char *strCount = token.substr(token.find(innerDelimeter) + 1, token.size()).c_str();
    int count = atoi(strCount);

    // Insert the counts following each collision
    outputIt = output.find(word);
    if (outputIt != output.end()) {
      outputIt->second.push_back(count);
    } else {
      vector<int> newIntList = {count};
      output.insert(pair<string, vector<int>>(word, newIntList));
    }

    words.erase(0, pos + delimiter.length());
  }

  return output;
}

map<string, int> countWordsFrom(map<string, vector<int>> hashTable) {
  map<string, int> output;
  map<string, int>::iterator outputIt;

  for (auto &[key, arrVal] : hashTable) {
    int count = 0;

    for (auto val : arrVal) {
      count += val;
    }

    outputIt = output.find(key);
    if (outputIt != output.end()) {
      outputIt->second += count;
    } else {
      output.insert(pair<string, int>(key, count));
    }
  }

  return output;
}

int main(int argc, char *argv[]) {
  MPI_Init(NULL, NULL);

  const int root = 0;
  int my_rank = 0, world_size;
  MPI_Comm_rank(MPI_COMM_WORLD, &my_rank);
  MPI_Comm_size(MPI_COMM_WORLD, &world_size);

  char buff[50];
  map<string, int> words = {};
  map<string, int>::iterator wordsIt;

  // Prepare each file reading
  FILE *file;

  const char *fileNamePrefixRaw = to_string(my_rank + 1).c_str();
  char *fileNamePrefix = strdup(fileNamePrefixRaw);
  char *fileNameDirectory = strdup("./files/");
  char *fileName = strcat(fileNameDirectory, strcat(fileNamePrefix, ".txt"));

  file = fopen(fileName, "r");

  // Read respective file
  while (fscanf(file, "%s", buff) == 1) {
    string readWord = convertToString(buff);
    removeNonAlphanumeric(&readWord);

    wordsIt = words.find(readWord);
    if (wordsIt != words.end()) {
      wordsIt->second++;
    } else {
      words.insert(pair<string, int>(readWord, 1));
    }
  }

  // Prepare to send to root
  string completeList = serializeWords(words);
  int listSize = completeList.size();

  // Gather count of chars
  int *numChars = NULL;
  if (my_rank == root) {
    numChars = (int *)malloc(world_size * sizeof(int));
  }

  MPI_Gather(&listSize, 1, MPI_INT, numChars, 1, MPI_INT, root, MPI_COMM_WORLD);

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
  MPI_Gatherv(completeList.c_str(), listSize, MPI_CHAR,
              totalstring, numChars, displs, MPI_CHAR,
              root, MPI_COMM_WORLD);

  map<string, vector<int>> hashTable;

  if (my_rank == root) {
    cout << totalstring << endl;
    hashTable = deserializeWords(totalstring);

    map<string, int> wordsCounted = countWordsFrom(hashTable);

    for (auto const &[key, val] : wordsCounted) {
      cout << key << ": " << val << endl;
    }

    free(totalstring);
    free(displs);
    free(numChars);
  }

  fclose(file);

  MPI_Barrier(MPI_COMM_WORLD);
  MPI_Finalize();

  return 0;
}