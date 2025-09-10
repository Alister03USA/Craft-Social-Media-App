package coms309.people;

import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Controller used to showcase Create and Read from a LIST
 *
 * @author Vivek Bengre
 */

@RestController
public class BuildingController {

    HashMap<String, Building> buildingList = new  HashMap<>();

    // THIS IS THE LIST OPERATION
    // gets all the buildings in the list and returns it in JSON format
    @GetMapping("/building")
    public  HashMap<String,Building> getAllBuildings() {
        return buildingList;
    }

    // THIS IS THE CREATE OPERATION
    // springboot automatically converts JSON input into a building object and
    // the method below enters it into the list.
    @PostMapping("/building")
    public  String createBuilding(@RequestBody Building building) {
        System.out.println(building);
        buildingList.put(building.getName(), building);
        return "New building "+ building.getName() + " Saved";
    }

    // THIS IS THE READ OPERATION
    // Springboot gets the PATHVARIABLE from the URL
    // We extract the building from the HashMap.
    @GetMapping("/building/{name}")
    public Building getBuilding(@PathVariable String name) {
        Building b = buildingList.get(name);
        return b;
    }

    // THIS IS A GET METHOD
    // RequestParam is expected from the request under the key "name"
    // returns all names that contains value passed to the key "name"
    @GetMapping("/building/contains")
    public List<Building> getBuildingByParam(@RequestParam("name") String name) {
        List<Building> res = new ArrayList<>();
        for (Building b : buildingList.values()) {
            if (b.getName().contains(name))
                res.add(b);
        }
        return res;
    }

    // THIS IA A GET METHOD
    // We get just the address for a specific building
    @GetMapping("/building/{name}/address")
    public String getBuildingAddress(@PathVariable String name) {
        Building b = buildingList.get(name);
        return b.getAddress();
    }

    // THIS IS THE UPDATE OPERATION
    // We extract the building from the HashMap and modify it.
    @PutMapping("/building/{name}")
    public Building updateBuilding(@PathVariable String name, @RequestBody Building b) {
        buildingList.replace(name, b);
        return buildingList.get(name);
    }

    // THIS IS THE DELETE OPERATION
    @DeleteMapping("/building/{name}")
    public HashMap<String, Building> deleteBuilding(@PathVariable String name) {
        buildingList.remove(name);
        return buildingList;
    }
}

