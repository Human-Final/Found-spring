package com.human.found.domain.found.servuce;

import java.util.List;

import org.springframework.stereotype.Service;

import com.human.found.domain.found.mapper.FoundMapper;
import com.human.found.domain.found.vo.FoundVO;

import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class FoundServiceImpl implements FoundService {
    private final FoundMapper foundMapper;

    @Override
    public void Register(FoundVO foundVO) {
        foundMapper.insertfound(foundVO);
    }

    @Override
    public List<FoundVO> getFoundList() {
        return foundMapper.selectFoundList();
    }

}
