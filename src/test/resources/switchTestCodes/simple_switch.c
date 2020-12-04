int testFunction(int n)
{
int a = 0, b = 1;
switch (n) {
    case 0:
        n++;
        b = a + 1;
        a--;
        break;
    case 1:
        b = a + 2;
        return b;
    case 2:
        a++;
        break;
    default:
        break;
}
if (a>b)
    return a;
return b;
}