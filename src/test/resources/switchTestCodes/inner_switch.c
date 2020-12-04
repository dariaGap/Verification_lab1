int testFunction(int n)
{
int a = 0, b = 1;
switch (n) {
    case 0:
        switch (b) {
            case 0:
                b = a + 1;
                break;
            default:
                b = a - 1;
                break;
        }
        break;
    case 1:
        b = a + 2;
        return b;
    default:
        a++;
        break;
}
switch (a) {
    case 0:
        a++;
        break;
    default:
        break;
}
return a > b;
}