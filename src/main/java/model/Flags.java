package model;

public class Flags {
    private boolean functionDeclarationFlag = false,
            functionParametersFlag = false,
            selectionEndFlag = false,
            iterationFlag = false,
            iterationCounterFlag = false,
            iterationStartFlag = false,
            iterationEndFlag = false,
            switchStartFlag = false,
            switchEndFlag = false,
            isDeclarator = false;

    public boolean isFunctionDeclarationFlag() {
        return functionDeclarationFlag;
    }

    public void setFunctionDeclarationFlag(boolean functionDeclarationFlag) {
        this.functionDeclarationFlag = functionDeclarationFlag;
    }

    public boolean isFunctionParametersFlag() {
        return functionParametersFlag;
    }

    public void setFunctionParametersFlag(boolean functionParametersFlag) {
        this.functionParametersFlag = functionParametersFlag;
    }

    public boolean isSelectionEndFlag() {
        return selectionEndFlag;
    }

    public void setSelectionEndFlag(boolean selectionEndFlag) {
        this.selectionEndFlag = selectionEndFlag;
    }

    public boolean isIterationFlag() {
        return iterationFlag;
    }

    public void setIterationFlag(boolean iterationFlag) {
        this.iterationFlag = iterationFlag;
    }

    public boolean isIterationCounterFlag() {
        return iterationCounterFlag;
    }

    public void setIterationCounterFlag(boolean iterationCounterFlag) {
        this.iterationCounterFlag = iterationCounterFlag;
    }

    public boolean isIterationStartFlag() {
        return iterationStartFlag;
    }

    public void setIterationStartFlag(boolean iterationStartFlag) {
        this.iterationStartFlag = iterationStartFlag;
    }

    public boolean isIterationEndFlag() {
        return iterationEndFlag;
    }

    public void setIterationEndFlag(boolean iterationEndFlag) {
        this.iterationEndFlag = iterationEndFlag;
    }

    public boolean isSwitchStartFlag() {
        return switchStartFlag;
    }

    public  void setSwitchStartFlag(boolean switchStartFlag) {
        this.switchStartFlag = switchStartFlag;
    }

    public boolean isSwitchEndFlag() {
        return switchEndFlag;
    }

    public void setSwitchEndFlag(boolean switchEndFlag) {
        this.switchEndFlag = switchEndFlag;
    }

    public boolean isDeclarator() {
        return isDeclarator;
    }

    public void setDeclarator(boolean declarator) {
        isDeclarator = declarator;
    }
}
