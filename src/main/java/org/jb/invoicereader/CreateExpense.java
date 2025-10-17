package org.jb.invoicereader;

import tools.jackson.databind.node.ObjectNode;

public class CreateExpense {
    private ConexosAPI conexosApi = ConexosAPI.getInstance();

    public CreateExpense(ObjectNode data) {
        System.out.println(data);
    }

    private boolean addSP() {
        return true;
    }

    private boolean generateSP() {
        return true;
    }

    private boolean getAddressCode() {
        return true;
    }
}
