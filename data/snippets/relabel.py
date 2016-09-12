data = open('test.txt','r')
ndata = open('test2.txt','w')

for line in data:
	lbl = line.split(" ")[-1]
	line = lbl[:len(lbl)-1] + ":" + line[0:len(line)-len(lbl)] + '\n'
	ndata.write(line)
ndata.close()