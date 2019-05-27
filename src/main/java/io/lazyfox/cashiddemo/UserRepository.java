package io.lazyfox.cashiddemo;

import org.springframework.data.repository.CrudRepository;

/**
 * Copyright (c) 2019 FoxTalk Ltd
 * 
 * Distributed under the MIT software license, see the accompanying file LICENSE
 * or http://www.opensource.org/licenses/mit-license.php.
 */
public interface UserRepository extends CrudRepository<User, Long> {

	User findFirstByBchAddress(String bchAddress);

	User findFirstById(Long id);

	User findFirstByName(String name);

}
