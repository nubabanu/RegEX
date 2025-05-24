#!/bin/zsh
# Navigate to the root project directory (parent of SoSe25)
cd "$(dirname "$0")/.."
mvn javafx:run
