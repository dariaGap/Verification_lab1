int testFunction(int n,int i)
{
int b = 10;
switch (n) {
    case 0:
        if(b>i) {
            i++;
            b--;
        } else {
            i--;
            b++;
        }
        break;
    case 1:
        if(b>i) {
            b++;
            i--;
        } else {
            b--;
            i++;
        }
        break;
}

return n + i + b;
}