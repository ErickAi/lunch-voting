package com.example.web;

import com.example.dao.RestaurantRepository;
import com.example.domain.Restaurant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

import static com.example.util.ValidationUtil.assureIdConsistent;

@RestController
@Transactional(readOnly = true)
@RequestMapping(value = RestaurantRestController.RESTAURANT_URL, produces = MediaType.APPLICATION_JSON_VALUE)
public class RestaurantRestController {

    public static final String RESTAURANT_URL = "api/restaurants";
    private final Logger log = LoggerFactory.getLogger(getClass());
    private static final Sort SORT_NAME = new Sort(Sort.Direction.ASC, "name");

    @Autowired
    private RestaurantRepository repository;


    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Restaurant> create(@RequestBody Restaurant restaurant) {
        Restaurant created = repository.save(restaurant);
        URI uriOfNewResource = ServletUriComponentsBuilder.fromCurrentContextPath()
                .path(RESTAURANT_URL + "/{id}")
                .buildAndExpand(created.getId()).toUri();
        return ResponseEntity.created(uriOfNewResource).body(created);
    }

    @GetMapping
    public List<Restaurant> getAll() {
        log.info("getAll");
        return repository.findAll(SORT_NAME);
    }

    @Transactional(readOnly = true)
    @GetMapping(value = "/{id}")
    public ResponseEntity<Restaurant> get(@PathVariable("id") int id) {
        log.info("get ({})", id);
        return repository.findById(id)
                .map(restaurant -> new ResponseEntity<>(restaurant, HttpStatus.OK))
                .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @PutMapping(value = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE)
    public void update(@RequestBody Restaurant restaurant, @PathVariable("id") int id) {
        log.info("update {} with id={}", restaurant, id);
        assureIdConsistent(restaurant, id);
        repository.save(restaurant);
    }

    @DeleteMapping(value = "/{id}")
    @ResponseStatus(value = HttpStatus.NO_CONTENT)
    public void delete(@PathVariable("id") int id) {
        log.info("delete ({})", id);
        repository.deleteById(id);
    }

    @GetMapping(value = "/find-by-name")
    public ResponseEntity<List<Restaurant>> findByName(@RequestParam("name") String name) {
        List<Restaurant> found = repository.findByName(name);
        if (found.size() > 0)
            return new ResponseEntity<>(found, HttpStatus.OK);
        else return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
