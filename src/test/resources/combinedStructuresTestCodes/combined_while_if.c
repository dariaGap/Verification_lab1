int testFunction(int n,int i)
{
int b = 10;
while (i < b) {
    if (i < n)
       i++;
    else
       n++;
}
return n + i + b;
}