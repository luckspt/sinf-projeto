dd if=/dev/zero of=$1 bs=$1 seek=1 count=0
echo "o meu ip é: " >> $1
ip addr | grep /22 >> $1