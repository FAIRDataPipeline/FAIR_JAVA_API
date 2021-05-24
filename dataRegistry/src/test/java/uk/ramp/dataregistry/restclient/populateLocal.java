package uk.ramp.dataregistry.restclient;

import jakarta.ws.rs.core.GenericType;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.condition.DisabledIf;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import uk.ramp.dataregistry.content.Storage_location;
import uk.ramp.dataregistry.content.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.stream.Stream;

import jakarta.ws.rs.NotFoundException;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class populateLocal {
    RestClient lc;
    String localReg = "http://localhost:8000/api/";

    @BeforeAll
    public void setUp() throws Exception {
        lc = new RestClient(localReg);
    }

    @Test
    public void get_NonExistentNamespace() {
        Assertions.assertThrows(NotFoundException.class, () -> {lc.get(Namespace.class, 9999);});
    }

    @ParameterizedTest
    @MethodSource("objectsToBeCreated")
    @DisabledIf("storageRootExists")
    public void createObjects(FDP_Updateable o) {
        lc.post(o);
    }

    private Stream<FDP_Updateable> objectsToBeCreated() {
        ArrayList<FDP_Updateable> al = new ArrayList<FDP_Updateable>();
        Storage_root sr = new Storage_root();
        sr.setName("Initial storage root");
        sr.setRoot("D:\\DataStore");
        al.add(sr);
        Storage_location sl = new Storage_location();
        sl.setPath("OnePath");
        sl.setStorage_root(localReg + "storage_root/1");
        al.add(sl);

        return al.stream();
    }

    private boolean storageRootExists(){
        try {
            GenericType gt = new GenericType<FDP_ObjectList<Storage_root>>() {};

            FDP_ObjectList<Storage_root> sr = (FDP_ObjectList<Storage_root>) lc.getList(gt, new HashMap<String, String>());
            System.out.println("storageRootExists? " + (sr.getCount() != null));
            return sr != null;
        } catch (NotFoundException e) {
            System.out.println("storageRootExists? NotFoundException: return false.");
            return false;
        }
    }




}
