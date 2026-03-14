package top.wjc.bookmallbackend.service;

import top.wjc.bookmallbackend.dto.AddressCreateRequest;
import top.wjc.bookmallbackend.dto.AddressUpdateRequest;
import top.wjc.bookmallbackend.vo.AddressVO;

import java.util.List;

public interface AddressService {
    List<AddressVO> list(Long userId);

    void create(Long userId, AddressCreateRequest request);

    void update(Long userId, Long addressId, AddressUpdateRequest request);

    void delete(Long userId, Long addressId);

    void setDefault(Long userId, Long addressId);
}
