int testFunction(int n)
{
int a = 0, b = 1, c = a + b;
int i = 0, j = 0;
while(i<n) {
    i++;
    while(j<i) {
        j++;
        c++;
        if (c > a)
            continue;
        b = c + a;
        if (b > a)
            break;
    }
    c = a + b;
    if (c > a)
        continue;
    else
        a = b + c;
}
return a + b + c;
}