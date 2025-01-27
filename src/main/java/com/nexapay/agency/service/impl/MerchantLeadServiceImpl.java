package com.nexapay.agency.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.nexapay.agency.common.R;
import com.nexapay.agency.dto.agency.CompanyInfoDTO;
import com.nexapay.agency.dto.agency.PersonalInfoDTO;
import com.nexapay.agency.dto.merchant.MerchantLeadDTO;
import com.nexapay.agency.dto.merchant.MerchantLeadRequest;
import com.nexapay.agency.dto.merchant.MerchantLeadStats;
import com.nexapay.agency.dto.merchant.PageResponse;
import com.nexapay.agency.entity.AgencyKyc;
import com.nexapay.agency.entity.AgencyUser;
import com.nexapay.agency.entity.MerchantLead;
import com.nexapay.agency.mapper.AgencyKycMapper;
import com.nexapay.agency.mapper.AgencyUserMapper;
import com.nexapay.agency.mapper.MerchantLeadMapper;
import com.nexapay.agency.service.MerchantLeadService;
import lombok.RequiredArgsConstructor;

import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class MerchantLeadServiceImpl implements MerchantLeadService {
    private final AgencyUserMapper agencyUserMapper;
    private final MerchantLeadMapper merchantLeadMapper;
    private final AgencyKycMapper agencyKycMapper;
    private final ObjectMapper objectMapper;
    @Override
    public R<AgencyUser> getAgencyByInviteCode(String inviteCode) {
        AgencyUser agencyUser = agencyUserMapper.selectOne(
                new LambdaQueryWrapper<AgencyUser>()
                        .eq(AgencyUser::getInviteCode, inviteCode)
        );

        if (agencyUser == null) {
            return R.error("Invalid invite code");
        }
        AgencyUser agencyUser1 = new AgencyUser();
        agencyUser1.setId(agencyUser.getId());
        agencyUser1.setInviteCode(agencyUser.getInviteCode());
        return R.success(agencyUser1);
    }

    @Override
    public R<MerchantLeadDTO> register(MerchantLeadRequest.Register request) {
        // Verify invite code
        AgencyUser agencyUser = agencyUserMapper.selectOne(
                new LambdaQueryWrapper<AgencyUser>()
                        .eq(AgencyUser::getInviteCode, request.getInviteCode())
        );
        if (agencyUser == null) {
            return R.error("Invalid invite code");
        }

        // Check if email already exists
        MerchantLead existingLead = merchantLeadMapper.selectOne(
                new LambdaQueryWrapper<MerchantLead>()
                        .eq(MerchantLead::getEmail, request.getEmail())
        );
        if (existingLead != null) {
            return R.error("您已经登记过，请不要重复登记");
        }

        // Create new lead
        MerchantLead lead = new MerchantLead();
        lead.setAgencyId(agencyUser.getId());
        lead.setInviteCode(request.getInviteCode());
        lead.setEmail(request.getEmail());
        lead.setPhone(request.getPhone());
        lead.setWechat(request.getWechat());
        lead.setStatus(0); // pending
        lead.setCreateTime(LocalDateTime.now());
        lead.setUpdateTime(LocalDateTime.now());

        merchantLeadMapper.insert(lead);
        return R.success(convertToDTO(lead));
    }

    @Override
    public R<MerchantLeadDTO> update(MerchantLeadRequest.Update request) {
        MerchantLead lead = merchantLeadMapper.selectById(request.getId());
        if (lead == null) {
            return R.error("Lead not found");
        }


        lead.setPhone(request.getPhone());
        lead.setWechat(request.getWechat());
        lead.setUpdateTime(LocalDateTime.now());

        merchantLeadMapper.updateById(lead);
        return R.success(convertToDTO(lead));
    }

    @Override
    public R<PageResponse<MerchantLeadDTO>> list(Integer page, Integer size) {
        page = (page == null || page < 1) ? 1 : page;
        size = (size == null || size < 1) ? 10 : size;

        Page<MerchantLead> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<MerchantLead> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.orderByDesc(MerchantLead::getCreateTime);

        Page<MerchantLead> leadPage = merchantLeadMapper.selectPage(pageParam, queryWrapper);
        Long total = merchantLeadMapper.selectCount(queryWrapper);

        // Calculate stats
        MerchantLeadStats stats = new MerchantLeadStats();
        stats.setTotalLeads(total);

        // New leads this month
        LocalDateTime firstDayOfMonth = LocalDateTime.now().withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0);
        stats.setNewLeadsThisMonth(merchantLeadMapper.selectCount(
                new LambdaQueryWrapper<MerchantLead>()
                        .ge(MerchantLead::getCreateTime, firstDayOfMonth)
        ));

        // In progress leads (status = 1)
        stats.setInProgressLeads(merchantLeadMapper.selectCount(
                new LambdaQueryWrapper<MerchantLead>()
                        .eq(MerchantLead::getStatus, 1)
        ));

        // Converted leads (status = 2)
        stats.setConvertedLeads(merchantLeadMapper.selectCount(
                new LambdaQueryWrapper<MerchantLead>()
                        .eq(MerchantLead::getStatus, 2)
        ));

        // Create DTO page
        Page<MerchantLeadDTO> dtoPage = new Page<>();
        dtoPage.setCurrent(page);
        dtoPage.setSize(size);
        dtoPage.setTotal(total);
        dtoPage.setPages((total + size - 1) / size);

        List<MerchantLeadDTO> dtoList = leadPage.getRecords().stream().map(lead -> {
            MerchantLeadDTO dto = new MerchantLeadDTO();
            BeanUtils.copyProperties(lead, dto);

            AgencyKyc agencyKyc = agencyKycMapper.selectOne(
                    new LambdaQueryWrapper<AgencyKyc>()
                            .eq(AgencyKyc::getUserId, lead.getAgencyId())
                            .orderByDesc(AgencyKyc::getCreatedAt)
                            .last("LIMIT 1")
            );

            if (agencyKyc != null) {
                try {
                    if ("personal".equals(agencyKyc.getType())) {
                        PersonalInfoDTO personalInfo = objectMapper.readValue(
                                agencyKyc.getPersonalInfo(), PersonalInfoDTO.class);
                        dto.setAgencyName(personalInfo.getName());
                    } else {
                        CompanyInfoDTO companyInfo = objectMapper.readValue(
                                agencyKyc.getCompanyInfo(), CompanyInfoDTO.class);
                        dto.setAgencyName(companyInfo.getCompanyName());
                    }
                } catch (JsonProcessingException e) {
                    log.error("Error parsing JSON for agency: " + lead.getAgencyId(), e);
                }
            }

            return dto;
        }).collect(Collectors.toList());

        dtoPage.setRecords(dtoList);

        return R.success(new PageResponse<>(dtoPage, stats));
    }
    @Override
    public R<MerchantLeadDTO> updateStatus(MerchantLeadRequest.UpdateStatus request) {
        MerchantLead lead = merchantLeadMapper.selectById(request.getId());
        if (lead == null) {
            return R.error("Lead not found");
        }

        lead.setStatus(request.getStatus());
        lead.setUpdateTime(LocalDateTime.now());

        merchantLeadMapper.updateById(lead);
        return R.success(convertToDTO(lead));
    }

    private MerchantLeadDTO convertToDTO(MerchantLead lead) {
        MerchantLeadDTO dto = new MerchantLeadDTO();
        BeanUtils.copyProperties(lead, dto);
        return dto;
    }

    @Override
    public R<MerchantLeadDTO> updateSales(MerchantLeadRequest.UpdateSales request) {
        MerchantLead lead = merchantLeadMapper.selectById(request.getId());
        if (lead == null) {
            return R.error("Lead not found");
        }

        lead.setSalesName(request.getSalesName());
        lead.setUpdateTime(LocalDateTime.now());

        merchantLeadMapper.updateById(lead);
        return R.success(convertToDTO(lead));
    }
}