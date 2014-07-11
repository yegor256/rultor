#!/bin/sh
BIN=script.sh
echo "#!/bin/bash" > ${BIN}
echo "set -x" >> ${BIN}
echo "set -e" >> ${BIN}
echo "set -o pipefail" >> ${BIN}
echo "cd repo" >> ${BIN}
echo "${SCRIPT[@]}" >> ${BIN}
chmod a+x ${BIN}
