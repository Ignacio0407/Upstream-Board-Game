package es.us.dp1.l4_01_24_25.upstream.statistic;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.validation.Valid;

@Service
public class AchievementService {
        
    AchievementRepository repo;

    @Autowired
    public AchievementService(AchievementRepository repo){
        this.repo=repo;
    }

    @Transactional(readOnly = true)    
    List<Achievement> getAchievements(){
        return repo.findAll();
    }
    
    @Transactional(readOnly = true)    
    public Achievement getById(int id){
        Optional<Achievement> result=repo.findById(id);
        return result.isPresent()?result.get():null;
    }

    @Transactional(readOnly = true)    
    public Achievement getByName(String name){
        Optional<Achievement> result = Optional.ofNullable(repo.findByName(name));
        return result.isPresent()?result.get():null;
    }

    @Transactional(readOnly = true)    
    public List<Achievement> getByNames(List<String> names) {
        List<Achievement> result = new LinkedList<>();
        names.forEach(name -> result.add(repo.findByName(name)));
        return result.size() == names.size() ? new ArrayList<>(result) : null;
    }

    @Transactional
    public Achievement saveAchievement(@Valid Achievement newAchievement) {
        return repo.save(newAchievement);
    }

    
    @Transactional
    public void deleteAchievementById(int id){
        repo.deleteById(id);
    }
    

}
