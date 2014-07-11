#!/bin/sh
bin=script.sh
echo "#!/bin/bash" > ${bin}
echo "set -x" >> ${bin}
echo "set -e" >> ${bin}
echo "set -o pipefail" >> ${bin}
echo "cd repo" >> ${bin}
echo "${scripts[@]}" >> ${bin}
chmod a+x ${bin}

