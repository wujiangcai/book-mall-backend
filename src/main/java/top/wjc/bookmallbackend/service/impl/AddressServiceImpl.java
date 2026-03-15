package top.wjc.bookmallbackend.service.impl;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import top.wjc.bookmallbackend.dto.AddressCreateRequest;
import top.wjc.bookmallbackend.dto.AddressUpdateRequest;
import top.wjc.bookmallbackend.entity.Address;
import top.wjc.bookmallbackend.exception.BusinessException;
import top.wjc.bookmallbackend.exception.NotFoundException;
import top.wjc.bookmallbackend.mapper.AddressMapper;
import top.wjc.bookmallbackend.mapper.OrderMapper;
import top.wjc.bookmallbackend.service.AddressService;
import top.wjc.bookmallbackend.vo.AddressVO;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class AddressServiceImpl implements AddressService {

    private final AddressMapper addressMapper;
    private final OrderMapper orderMapper;

    public AddressServiceImpl(AddressMapper addressMapper, OrderMapper orderMapper) {
        this.addressMapper = addressMapper;
        this.orderMapper = orderMapper;
    }

    @Override
    public List<AddressVO> list(Long userId) {
        return addressMapper.selectByUserId(userId).stream()
                .map(address -> new AddressVO(
                        address.getId(),
                        address.getReceiverName(),
                        address.getPhone(),
                        address.getProvince(),
                        address.getCity(),
                        address.getDistrict(),
                        address.getDetailAddress(),
                        address.getIsDefault()
                ))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void create(Long userId, AddressCreateRequest request) {
        boolean shouldDefault = request.getIsDefault() != null && request.getIsDefault() == 1;
        if (addressMapper.countDefaultByUserId(userId) == 0) {
            shouldDefault = true;
        }
        if (shouldDefault) {
            addressMapper.clearDefaultByUserId(userId);
        }
        Address address = Address.builder()
                .userId(userId)
                .receiverName(request.getReceiverName())
                .phone(request.getPhone())
                .province(request.getProvince())
                .city(request.getCity())
                .district(request.getDistrict())
                .detailAddress(request.getDetailAddress())
                .isDefault(shouldDefault ? 1 : 0)
                .build();
        addressMapper.insert(address);
    }

    @Override
    @Transactional
    public void update(Long userId, Long addressId, AddressUpdateRequest request) {
        Address existing = addressMapper.selectById(addressId);
        if (existing == null || !existing.getUserId().equals(userId)) {
            throw new NotFoundException();
        }
        Address address = Address.builder()
                .id(addressId)
                .userId(userId)
                .receiverName(request.getReceiverName())
                .phone(request.getPhone())
                .province(request.getProvince())
                .city(request.getCity())
                .district(request.getDistrict())
                .detailAddress(request.getDetailAddress())
                .isDefault(existing.getIsDefault())
                .build();
        addressMapper.update(address);
    }

    @Override
    @Transactional
    public void delete(Long userId, Long addressId) {
        Address existing = addressMapper.selectById(addressId);
        if (existing == null || !existing.getUserId().equals(userId)) {
            throw new NotFoundException();
        }
        if (orderMapper.countByAddressId(addressId) > 0) {
            throw new BusinessException(400, "地址已被订单使用，无法删除");
        }
        addressMapper.deleteById(addressId);
        if (existing.getIsDefault() != null && existing.getIsDefault() == 1) {
            Address latest = addressMapper.selectLatestByUserId(userId);
            if (latest != null) {
                addressMapper.clearDefaultByUserId(userId);
                addressMapper.updateDefault(Address.builder()
                        .id(latest.getId())
                        .isDefault(1)
                        .build());
            }
        }
    }

    @Override
    @Transactional
    public void setDefault(Long userId, Long addressId) {
        Address existing = addressMapper.selectById(addressId);
        if (existing == null || !existing.getUserId().equals(userId)) {
            throw new NotFoundException();
        }
        addressMapper.clearDefaultByUserId(userId);
        addressMapper.updateDefault(Address.builder()
                .id(existing.getId())
                .isDefault(1)
                .build());
    }
}
